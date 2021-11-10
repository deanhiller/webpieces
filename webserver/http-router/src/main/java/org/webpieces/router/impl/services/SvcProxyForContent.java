package org.webpieces.router.impl.services;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
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
import org.webpieces.util.exceptions.SneakyThrow;
import org.webpieces.util.filters.Service;
import org.webpieces.util.futures.FutureHelper;

public class SvcProxyForContent implements Service<MethodMeta, Action> {

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
		
		return futureArgs.thenCompose( argsResult -> invokeAndCoerce(meta, info, m, argsResult));
	}

	private List<Object> validate(Object controller, Method m, List<Object> args) {
		List<Violation> violations = validator.validate(controller, m, args);

		if(violations.size() > 0) {
			throw new BadRequestException(violations);
		}
		
		return args;
	}

	private XFuture<Action> invokeAndCoerce(MethodMeta meta, RouteInfoForContent info, Method m,
			List<Object> argsResult) {
		Object retVal;
		try {
			retVal = invoker.invokeController(meta.getLoadedController(), argsResult.toArray());
		} catch (InvocationTargetException e) {
			if(e.getCause() instanceof WebpiecesException)
				throw (WebpiecesException)e.getCause();
			throw SneakyThrow.sneak(e);
		} catch (IllegalAccessException e) {
			throw SneakyThrow.sneak(e);
		}
		
		if(info.getBodyContentBinder() != null)
			return unwrapResult(m, retVal, info.getBodyContentBinder());

		return invoker.coerceReturnValue(retVal);
	}

	@SuppressWarnings("unchecked")
	private XFuture<Action> unwrapResult(Method method, Object retVal, BodyContentBinder binder) {
		Class<?> returnType = method.getReturnType();
		
		if(XFuture.class.isAssignableFrom(returnType)) {
			if(retVal == null)
				throw new IllegalStateException("Your method returned a null XFuture which it not allowed.  method="+method);
			XFuture<Object> future = (XFuture<Object>) retVal;
			return future.thenApply((bean) -> marshal(method, binder, bean));
		} else {
			RenderContent content = marshal(method, binder, retVal);
			//binder.marshal(retVal);
			return XFuture.completedFuture(content);
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
