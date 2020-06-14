package org.webpieces.router.impl.routeinvoker;

import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.Messages;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.api.streams.StreamService;
import org.webpieces.router.impl.loader.ControllerLoader;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.routers.Endpoint;
import org.webpieces.router.impl.services.RouteData;
import org.webpieces.router.impl.services.RouteInfoForStatic;
import org.webpieces.util.futures.FutureHelper;

import com.google.inject.ImplementedBy;

@ImplementedBy(ProdRouteInvoker.class)
public abstract class AbstractRouteInvoker implements RouteInvoker {

	protected final ControllerLoader controllerFinder;
	
	protected FutureHelper futureUtil;
	private RouteInvokerStatic staticInvoker;
	private ServiceInvoker serviceInvoker;

	public AbstractRouteInvoker(
			ControllerLoader controllerFinder,
			FutureHelper futureUtil,
			RouteInvokerStatic staticInvoker,
			ServiceInvoker serviceInvoker
	) {
		this.controllerFinder = controllerFinder;
		this.futureUtil = futureUtil;
		this.staticInvoker = staticInvoker;
		this.serviceInvoker = serviceInvoker;
	}
	
	@Override
	public RouterStreamRef invokeStatic(RequestContext ctx, ProxyStreamHandle handler, RouteInfoForStatic data) {
		return staticInvoker.invokeStatic(ctx, handler, data);
	}
	
	@Override
	public CompletableFuture<Void> invokeErrorController(InvokeInfo invokeInfo, Endpoint dynamicInfo, RouteData data) {
		ResponseProcessorAppError processor = new ResponseProcessorAppError();
		return invokeSvc(invokeInfo, dynamicInfo, data, processor);
	}
	
	@Override
	public CompletableFuture<Void> invokeNotFound(InvokeInfo invokeInfo, Endpoint dynamicInfo, RouteData data) {
		ResponseProcessorNotFound processor = new ResponseProcessorNotFound();
		return invokeSvc(invokeInfo, dynamicInfo, data, processor);	
	}
	
	private CompletableFuture<Void> invokeSvc(InvokeInfo invokeInfo, Endpoint dynamicInfo, RouteData data, Processor processor) {
		LoadedController loadedController = invokeInfo.getLoadedController();
		ProxyStreamHandle handle = invokeInfo.getHandler();
		RequestContext requestCtx = invokeInfo.getRequestCtx();
		MethodMeta methodMeta = new MethodMeta(loadedController, requestCtx, invokeInfo.getRouteType(), data);
		String i18nBundleName = "";
		return serviceInvoker.invokeSvc(methodMeta, i18nBundleName, dynamicInfo, processor, handle);
	}
	
	@Override
	public RouterStreamRef invokeHtmlController(InvokeInfo invokeInfo, StreamService dynamicInfo, RouteData data) {
		return invokeRealRoute(invokeInfo, dynamicInfo, data);
	}
	@Override
	public RouterStreamRef invokeContentController(InvokeInfo invokeInfo, StreamService dynamicInfo, RouteData data) {
		return invokeRealRoute(invokeInfo, dynamicInfo, data);
	}
	@Override
	public RouterStreamRef invokeStreamingController(InvokeInfo invokeInfo, StreamService dynamicInfo, RouteData data) {
		return invokeRealRoute(invokeInfo, dynamicInfo, data);
	}

	protected RouterStreamRef invokeRealRoute(InvokeInfo invokeInfo, StreamService endpoint, RouteData data) {
		if(endpoint == null)
			throw new IllegalStateException("Bug, service should never be null at this point");
		
		LoadedController loadedController = invokeInfo.getLoadedController();
		RequestContext requestCtx = invokeInfo.getRequestCtx();
		Messages messages = new Messages(invokeInfo.getI18nBundleName(), "webpieces");
		requestCtx.setMessages(messages);
		Current.setContext(requestCtx);
		MethodMeta methodMeta = new MethodMeta(loadedController, Current.getContext(), invokeInfo.getRouteType(), data);
		
		return endpoint.openStream(methodMeta, invokeInfo.getHandler());
	}
	
}
