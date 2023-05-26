package org.webpieces.router.impl.services;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import org.webpieces.router.api.RecordingInfo;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.util.context.Context;
import org.webpieces.util.futures.XFuture;

import org.webpieces.http.exception.BadRequestException;
import org.webpieces.http.exception.Violation;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.RenderContent;
import org.webpieces.router.api.exceptions.IllegalReturnValueException;
import org.webpieces.util.exceptions.WebpiecesException;
import org.webpieces.router.api.extensions.BodyContentBinder;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.impl.model.SvcProxyLogic;
import org.webpieces.router.impl.params.BeanValidator;
import org.webpieces.router.impl.params.ParamToObjectTranslatorImpl;
import org.webpieces.util.SneakyThrow;
import org.webpieces.util.filters.Service;
import org.webpieces.util.futures.FutureHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SvcProxyForContent implements Service<MethodMeta, Action> {

	private static final Logger log = LoggerFactory.getLogger(SvcProxyForContent.class);
	private final ParamToObjectTranslatorImpl translator;
	private final ControllerInvoker invoker;
	private FutureHelper futureUtil;
	private BeanValidator validator;

	public SvcProxyForContent(SvcProxyLogic svcProxyLogic, FutureHelper futureUtil) {
		this.futureUtil = futureUtil;
		this.translator = svcProxyLogic.getTranslator();
		this.invoker = svcProxyLogic.getServiceInvoker();
		this.validator = svcProxyLogic.getValidator();
	}

	@Override
	public XFuture<Action> invoke(MethodMeta meta) {
		return futureUtil.syncToAsyncException(() -> invokeMethod(meta));
	}

	private XFuture<Action> invokeMethod(MethodMeta meta)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		RouteInfoForContent info = (RouteInfoForContent) meta.getRoute();

		Method m = meta.getLoadedController().getControllerMethod();
		Object obj = meta.getLoadedController().getControllerInstance();

		//We chose to do this here so any filters ESPECIALLY API filters
		//can catch and translate api errors and send customers a logical response
		//On top of that ORM plugins can have a transaction filter and then in this
		//createArgs can look up the bean before applying values since it is in
		//the transaction filter
		XFuture<List<Object>> futureArgs = translator.createArgs(m, meta.getCtx(), info.getBodyContentBinder())
				.thenApply ( args -> validate(obj, m, args));

		return futureArgs.thenCompose( argsResult -> invokeAndCoerce(meta, info, argsResult));
	}

	private List<Object> validate(Object controller, Method m, List<Object> args) {
		List<Violation> violations = validator.validate(controller, m, args);

		if(violations.size() > 0) {
			throw new BadRequestException(violations);
		}

		return args;
	}

	private XFuture<Action> invokeAndCoerce(MethodMeta meta, RouteInfoForContent info, List<Object> argsResult) {
		Object retVal;
		LoadedController loadedController = meta.getLoadedController();
		Object[] args = argsResult.toArray();
		try {
			retVal = invoker.invokeController(loadedController, args);
		} catch (InvocationTargetException e) {
			if(e.getCause() instanceof ExecutionException)
			{
				Throwable cause = e.getCause();
				if (cause.getCause() instanceof WebpiecesException )
				{
					throw (WebpiecesException)cause.getCause();
				}
			}

			if(e.getCause() instanceof WebpiecesException){
				throw (WebpiecesException)e.getCause();
			}

			throw SneakyThrow.sneak(e);
		} catch (IllegalAccessException e) {
			throw SneakyThrow.sneak(e);
		}

		if(info.getBodyContentBinder() != null)
			return unwrapResult(loadedController, retVal, info.getBodyContentBinder(), args);

		return invoker.coerceReturnValue(retVal);
	}

	@SuppressWarnings("unchecked")
	private XFuture<Action> unwrapResult(LoadedController loadedController, Object retVal, BodyContentBinder binder, Object[] args) {
		Method method = loadedController.getControllerMethod();
		Class<?> returnType = method.getReturnType();

		if(XFuture.class.isAssignableFrom(returnType)) {
			if(retVal == null)
				throw new IllegalStateException("Your method returned a null XFuture which it not allowed.  method="+method);
			XFuture<Object> future = (XFuture<Object>) retVal;
			return future.handle((beanResp, e) -> marshalAndRecord(e, loadedController, binder, beanResp, args))
					.thenCompose(Function.identity());
		} else if(CompletableFuture.class.isAssignableFrom(returnType)) {
			if(retVal == null)
				throw new IllegalStateException("Your method returned a null XFuture which it not allowed.  method="+method);
			CompletableFuture<Object> future = (CompletableFuture<Object>) retVal;
			XFuture<Object> xFuture = XFuture.completedFuture(null).thenCompose((voi) -> future);
			return xFuture.handle((beanResp, e) -> marshalAndRecord(e, loadedController, binder, beanResp, args))
					.thenCompose(Function.identity());
		} else {
			RenderContent content = marshal(method, binder, retVal);
			//binder.marshal(retVal);
			return XFuture.completedFuture(content);
		}
	}

	private XFuture<Action> marshalAndRecord(Throwable t, LoadedController loadedController, BodyContentBinder binder, Object retVal, Object[] args) {
		Method method = loadedController.getControllerMethod();
		RecordingInfo recordingInfo = Context.get(RecordingInfo.JSON_ENDPOINT_RESULT);
		if(recordingInfo != null) {
			//if recording is on, set it up...
			recordingInfo.setMethod(method);
			recordingInfo.setArgs(args);
		}

		if(t != null) {
			if(recordingInfo != null)
				recordingInfo.setFailureResponse(t);
			return XFuture.failedFuture(t);
		}

		if(recordingInfo != null)
			recordingInfo.setResponse(retVal);

		//record
		try {
			RenderContent content = binder.marshal(retVal);
			return XFuture.completedFuture(content);
		} catch(RuntimeException e) {
			throw new IllegalReturnValueException("Exception marshaling retVal="+retVal+" from method="+method, e);
		}
	}

	private RenderContent marshal(Method method, BodyContentBinder binder, Object retVal) {
		try {
			return binder.marshal(retVal);
		} catch(RuntimeException e) {
			throw new IllegalReturnValueException("Exception marshaling retVal="+retVal+" from method="+method, e);
		}
	}
}
