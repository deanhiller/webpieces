package org.webpieces.router.impl.routeinvoker;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.Messages;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.router.api.RouterStreamHandle;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.exceptions.ControllerException;
import org.webpieces.router.api.exceptions.WebpiecesException;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.impl.ReverseRoutes;
import org.webpieces.router.impl.body.BodyParsers;
import org.webpieces.router.impl.loader.ControllerLoader;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.routebldr.BaseRouteInfo;
import org.webpieces.router.impl.routers.DynamicInfo;
import org.webpieces.router.impl.services.RouteData;
import org.webpieces.router.impl.services.RouteInfoForContent;
import org.webpieces.router.impl.services.RouteInfoForStatic;
import org.webpieces.util.filters.Service;
import org.webpieces.util.futures.FutureHelper;

import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.streaming.ResponseStreamHandle;
import com.webpieces.http2.api.streaming.StreamRef;
import com.webpieces.http2.api.streaming.StreamWriter;

public abstract class AbstractRouteInvoker implements RouteInvoker {

	protected final ControllerLoader controllerFinder;
	
	protected ReverseRoutes reverseRoutes;
	protected FutureHelper futureUtil;
	private BodyParsers requestBodyParsers;

	private RouteInvokerStatic staticInvoker;


	public AbstractRouteInvoker(
			ControllerLoader controllerFinder,
			FutureHelper futureUtil,
			RouteInvokerStatic staticInvoker,
			BodyParsers bodyParsers
	) {
		this.controllerFinder = controllerFinder;
		this.futureUtil = futureUtil;
		this.staticInvoker = staticInvoker;
		this.requestBodyParsers = bodyParsers;
	}

	@Override
	public void init(ReverseRoutes reverseRoutes) {
		this.reverseRoutes = reverseRoutes;
	}
	
	@Override
	public RouterStreamRef invokeStatic(RequestContext ctx, ProxyStreamHandle handler, RouteInfoForStatic data) {
		return staticInvoker.invokeStatic(ctx, handler, data);
	}
	
	@Override
	public CompletableFuture<StreamWriter> invokeErrorController(InvokeInfo invokeInfo, DynamicInfo dynamicInfo, RouteData data) {
		ResponseProcessorAppError processor = new ResponseProcessorAppError(
				invokeInfo.getRequestCtx(), dynamicInfo.getLoadedController(), invokeInfo.getHandler());
		
		CancelHolder cancelFunc = new CancelHolder(); //useless since not tied to RouterStreamRef but invoke error routes are quick anyways so no need to cancel
		return invokeImpl(invokeInfo, dynamicInfo, data, processor, cancelFunc, true); //true forces us to not wait and response ASAP since it's an error
	}
	
	@Override
	public CompletableFuture<StreamWriter> invokeNotFound(InvokeInfo invokeInfo, DynamicInfo dynamicInfo, RouteData data) {
		ResponseProcessorNotFound processor = new ResponseProcessorNotFound(
				invokeInfo.getRequestCtx(), 
				dynamicInfo.getLoadedController(), 
				invokeInfo.getHandler()
		);
		
		CancelHolder cancelFunc = new CancelHolder(); //useless since not tied to RouterStreamRef but invoke error routes are quick anyways so no need to cancel
		return invokeImpl(invokeInfo, dynamicInfo, data, processor, cancelFunc, true); //true forces us to not wait and response ASAP since it's an error
	}
	
	@Override
	public RouterStreamRef invokeHtmlController(InvokeInfo invokeInfo, DynamicInfo dynamicInfo, RouteData data) {
		ResponseProcessorHtml processor = new ResponseProcessorHtml(
				invokeInfo.getRequestCtx(), 
				dynamicInfo.getLoadedController(), invokeInfo.getHandler());
		return invokeRealRoute(invokeInfo, dynamicInfo, data, processor, false);
	}
	
	@Override
	public RouterStreamRef invokeContentController(InvokeInfo invokeInfo, DynamicInfo dynamicInfo, RouteData data) {
		RouteInfoForContent content = (RouteInfoForContent) data;
		if(content.getBodyContentBinder() == null)
			throw new IllegalArgumentException("bodyContentBinder is required for these routes yet it is null here.  bug");
		ResponseProcessorContent processor = new ResponseProcessorContent(invokeInfo.getRequestCtx(), invokeInfo.getHandler());
		return invokeRealRoute(invokeInfo, dynamicInfo, data, processor, false);
	}
	
	public RouterStreamRef invokeStreamingController(InvokeInfo invokeInfo, DynamicInfo dynamicInfo, RouteData data) {
		RequestContext requestCtx = invokeInfo.getRequestCtx();
		ProxyStreamHandle handler = invokeInfo.getHandler();
		LoadedController loadedController = dynamicInfo.getLoadedController();
		Object instance = loadedController.getControllerInstance();
		Method controllerMethod = loadedController.getControllerMethod();
		Parameter[] parameters = loadedController.getParameters();
		
		handler.initJustBeforeInvoke(reverseRoutes, invokeInfo, loadedController);

		Current.setContext(requestCtx);

		if(parameters.length != 1)
			throw new IllegalArgumentException("Your method='"+controllerMethod+"' MUST one parameter and does not.  It needs to take a RouterStreamHandler");
		else if(!ResponseStreamHandle.class.equals(parameters[0].getType()))
			throw new IllegalArgumentException("The single parameter must be RouterStreamHandle and was not for this method='"+controllerMethod+"'");
		else if(!StreamRef.class.equals(controllerMethod.getReturnType()))
			throw new IllegalArgumentException("The return value must be a subclass of StreamRef and was not for this method='"+controllerMethod+"'");

		
		RouterStreamRef streamRef = invokeStream(controllerMethod, instance, requestCtx, handler);
		CompletableFuture<StreamWriter> writer = streamRef.getWriter();
		
		CompletableFuture<StreamWriter> newFuture = futureUtil.catchBlockWrap(
				() -> writer,
				(t) -> convert(loadedController, t)
		); 

		//NO need for finally block
		Current.setContext(null);
		
		Function<CancelReason, CompletableFuture<Void>> cancelFunc = (reason) -> streamRef.cancel(reason);		
		return new RouterStreamRef("invokeStreaming", newFuture, cancelFunc);
	}
	
	public RouterStreamRef invokeStream(Method m, Object instance, RequestContext requestCtx, RouterStreamHandle handler) {
		try {
			StreamRef streamRef = (StreamRef) m.invoke(instance, handler);
			if(streamRef == null) {
				throw new IllegalStateException("You must return a non-null and did not from method='"+m+"'");
			}
			
			
			CompletableFuture<StreamWriter> newFuture = streamRef.getWriter().thenApply( resp -> {
				if(!(resp instanceof StreamWriter)) {
					throw new IllegalStateException("The return value must be StreamRef and was not for this method='"+m+"'");
				}
				return (StreamWriter) resp;
			});
			
			Function<CancelReason, CompletableFuture<Void>> cancelFunc = (reason) -> streamRef.cancel(reason);
			return new RouterStreamRef("invokeStreamController", newFuture, cancelFunc);
			
		} catch (Throwable e) {
			CompletableFuture<StreamWriter> failedFuture = futureUtil.failedFuture(e);
			return new RouterStreamRef("controllerFailed", failedFuture, null);
		}
	}

	protected RouterStreamRef invokeRealRoute(InvokeInfo invokeInfo, DynamicInfo dynamicInfo, RouteData data, Processor processor, boolean forceEndOfStream) {
		Service<MethodMeta, Action> service = dynamicInfo.getService();
		LoadedController loadedController = dynamicInfo.getLoadedController();
		invokeInfo.getHandler().initJustBeforeInvoke(reverseRoutes, invokeInfo, loadedController);

		CancelHolder cancelFunc = new CancelHolder();
		CompletableFuture<StreamWriter> writer =  invokeOnStreamComplete(invokeInfo, loadedController, service, data, processor, cancelFunc, forceEndOfStream);
	
		return new RouterStreamRef("invokeAny", writer, cancelFunc);		
	}
	
	protected CompletableFuture<StreamWriter> invokeImpl(InvokeInfo invokeInfo, DynamicInfo dynamicInfo, RouteData data, Processor processor, CancelHolder cancelFunc, boolean forceEndOfStream) {
		Service<MethodMeta, Action> service = dynamicInfo.getService();
		LoadedController loadedController = dynamicInfo.getLoadedController();
		invokeInfo.getHandler().initJustBeforeInvoke(reverseRoutes, invokeInfo, loadedController);

		return invokeOnStreamComplete(invokeInfo, loadedController, service, data, processor, cancelFunc, forceEndOfStream);
	}

	private CompletableFuture<StreamWriter> invokeOnStreamComplete(
			InvokeInfo invokeInfo,
			LoadedController loadedController,
			Service<MethodMeta, Action> service,
			RouteData data,
			Processor processor,
			CancelHolder cancelFunc,
			boolean forceEndOfStream) {
		
		boolean endOfStream = invokeInfo.getRequestCtx().getRequest().originalRequest.isEndOfStream();
		if(forceEndOfStream || endOfStream) {
			//If there is no body, just invoke to process OR IN CASE of InternalError or NotFound, there is NO need
			//to wait for the body and we can respond early, which stops wasting CPU of reading in their body
			invokeInfo.getRequestCtx().getRequest().body = DataWrapperGeneratorFactory.EMPTY;
			return invokeAnyImpl2(invokeInfo, loadedController, service, data, processor, cancelFunc).thenApply(voidd -> null);
		}
		
		//At this point, we don't have the end of the stream
		RequestStreamWriter2 writer = new RequestStreamWriter2(requestBodyParsers, invokeInfo,
				(newInfo) -> invokeAnyImpl2(newInfo, loadedController, service, data, processor, cancelFunc)
		);
		
		return CompletableFuture.completedFuture(writer);
	}
	 
	private CompletableFuture<Void> invokeAnyImpl2(
		InvokeInfo invokeInfo, 
		LoadedController loadedController, 
		Service<MethodMeta, Action> service,
		RouteData data, 
		Processor processor,
		CancelHolder cancelHolder
	) {
		BaseRouteInfo route = invokeInfo.getRoute();
		RequestContext requestCtx = invokeInfo.getRequestCtx();

		if(service == null)
			throw new IllegalStateException("Bug, service should never be null at this point");
		
		Messages messages = new Messages(route.getRouteModuleInfo().getI18nBundleName(), "webpieces");
		requestCtx.setMessages(messages);

		Current.setContext(requestCtx);
		
		MethodMeta methodMeta = new MethodMeta(loadedController, Current.getContext(), data);
		CompletableFuture<Action> response;
		try {
			response = futureUtil.catchBlockWrap( 
				() -> invokeService(service, methodMeta),
				(t) -> convert(loadedController, t)	
			);
		} finally {
			Current.setContext(null);
		}

		cancelHolder.setControllerFutureResponse(response);
		
		return response.thenCompose(resp -> continueProcessing(processor, requestCtx, resp));
	}

	private CompletableFuture<Void> continueProcessing(Processor processor, RequestContext requestCtx, Action resp) {
		return processor.continueProcessing(resp);
	}
	
	
	public CompletableFuture<Action> invokeService(Service<MethodMeta, Action> service, MethodMeta methodMeta) {
		return service.invoke(methodMeta);
	}
	
	private Throwable convert(LoadedController loadedController, Throwable t) {
		if(t instanceof WebpiecesException) {
			//MUST wrap with same exact exception so upper layers can detect and route
			WebpiecesException exc = (WebpiecesException) t;
			return exc.clone("exception occurred trying to invoke controller method(and filters)="+loadedController.getControllerMethod()); 
		}
		return new ControllerException("exception occurred on controller method="+loadedController.getControllerMethod(), t);
	}
	
}
