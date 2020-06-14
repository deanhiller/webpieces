package org.webpieces.devrouter.impl;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.ApplicationContext;
import org.webpieces.ctx.api.FlashSub;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.impl.WebInjector;
import org.webpieces.router.impl.body.BodyParsers;
import org.webpieces.router.impl.dto.RouteType;
import org.webpieces.router.impl.loader.ControllerLoader;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.router.impl.model.RouteModuleInfo;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.routebldr.RouteInfo;
import org.webpieces.router.impl.routeinvoker.AbstractRouteInvoker;
import org.webpieces.router.impl.routeinvoker.InvokeInfo;
import org.webpieces.router.impl.routeinvoker.RouteInvokerStatic;
import org.webpieces.router.impl.routeinvoker.RouterStreamRef;
import org.webpieces.router.impl.routers.Endpoint;
import org.webpieces.router.impl.routers.SimulateInternalError;
import org.webpieces.router.impl.services.RouteData;
import org.webpieces.router.impl.services.RouteInfoForInternalError;
import org.webpieces.router.impl.services.RouteInfoForNotFound;
import org.webpieces.router.impl.services.RouteInfoForStatic;
import org.webpieces.router.impl.services.ServiceInvoker;
import org.webpieces.router.impl.services.SvcProxyFixedRoutes;
import org.webpieces.util.futures.FutureHelper;

import com.google.inject.Injector;
import com.webpieces.http2.api.streaming.StreamWriter;

@Singleton
public class DevRouteInvoker extends AbstractRouteInvoker {
	private static final Logger log = LoggerFactory.getLogger(DevRouteInvoker.class);


	public final static String ERROR_KEY = "__webpiecesCompileError";

	
	private final ServiceInvoker serviceInvoker;
	private WebInjector webInjector;

	@Inject
	public DevRouteInvoker(
			WebInjector webInjector, 
			ControllerLoader loader, 
			ServiceInvoker invoker,
			FutureHelper futureUtil,
			RouteInvokerStatic staticInvoker,
			BodyParsers bodyParsers
	) {
		super(loader, futureUtil, staticInvoker, bodyParsers);
		this.webInjector = webInjector;
		this.serviceInvoker = invoker;
	}
	
	@Override
	public RouterStreamRef invokeStatic(RequestContext ctx, ProxyStreamHandle handle,
			RouteInfoForStatic data) {
		//RESET the encodings to known so we don't try to go the compressed cache which doesn't
		//exist in dev server since we want the latest files always
		ctx.getRequest().encodings = new ArrayList<>();
		return super.invokeStatic(ctx, handle, data);
	}

	@Override
	public RouterStreamRef invokeHtmlController(InvokeInfo invokeInfo, Endpoint dynamicInfo, RouteData data) {
		//special case for if stuff didn't compile and we flag it
		Throwable exc = (Throwable) invokeInfo.getRequestCtx().getRequest().requestState.get(ERROR_KEY);
		if(exc != null) {
			log.error("Could not compile your code", exc);
			RouteInfoForInternalError error = new RouteInfoForInternalError(exc); 
			CompletableFuture<StreamWriter> writer = invokeErrorController(invokeInfo, dynamicInfo, error);
			return new RouterStreamRef("notCompileError", writer, null);
		} if(invokeInfo.getRequestCtx().getRequest().queryParams.containsKey(DevelopmentController.INTERNAL_ERROR_KEY)) {
			//special case for in DevelopmentServer when invokeErrorController was called and then it's iframe called back out again to display
			//what would be displayed in production
			throw new SimulateInternalError(); //need to simulate the error to show production page
		}
		//ends up getting stuck ...need to rethink this..for now, screw it
//		else if(invokeInfo.getRequestCtx().getFlash().containsKey(DevelopmentController.WEBPIECES_EXCCEPTION_KEY)) {
//			//The user clicked refresh page so we have to intercept and show same issue
//			RouteInfoForInternalError error = new RouteInfoForInternalError(exc); 
//			CompletableFuture<StreamWriter> writer = invokeErrorController(invokeInfo, dynamicInfo, error);
//			return new RouterStreamRef("notCompileErrorRefresh", writer, null);			
//		}
		return super.invokeHtmlController(invokeInfo, dynamicInfo, data);
	}

	@Override
	public CompletableFuture<StreamWriter> invokeErrorController(InvokeInfo invokeInfo, Endpoint dynamicInfo,
			RouteData data) {
		RequestContext requestCtx = invokeInfo.getRequestCtx();
		ProxyStreamHandle handler = invokeInfo.getHandler();
		RouteInfoForInternalError error = (RouteInfoForInternalError)data;
		Throwable exception = error.getException();
		RouterRequest req = requestCtx.getRequest();
		
		if(exception instanceof SimulateInternalError) {
			//just use the original route at this point
			return super.invokeErrorController(invokeInfo, dynamicInfo, data);
		}
		
		Injector webAppInjector = webInjector.getCurrentInjector();

		RouteInfo routeInfo = new RouteInfo(new RouteModuleInfo("", null), "/org/webpieces/devrouter/impl/DevelopmentController.internalError");
		
		SvcProxyFixedRoutes svcProxy = new SvcProxyFixedRoutes(serviceInvoker, futureUtil);
		LoadedController newLoadedController = controllerFinder.loadGenericController(webAppInjector, routeInfo).getLoadedController();
		Endpoint newInfo = new Endpoint(svcProxy);
		
		RouterRequest newRequest = new RouterRequest();
		newRequest.putMultipart("url", req.relativePath);
		newRequest.isHttps = req.isHttps;
		newRequest.isBackendRequest = req.isBackendRequest;
		newRequest.originalRequest = req.originalRequest;
		newRequest.requestState.put(DevelopmentController.ORIGINAL_REQUEST, req);
		newRequest.requestState.put(DevelopmentController.EXCEPTION, exception);
		newRequest.requestState.put(DevRouteInvoker.ERROR_KEY, req.requestState.get(DevRouteInvoker.ERROR_KEY));
		
		ApplicationContext ctx = webInjector.getAppContext();
		RequestContext overridenCtx = new RequestContext(requestCtx.getValidation(), (FlashSub) requestCtx.getFlash(), requestCtx.getSession(), newRequest, ctx);
		InvokeInfo newInvokeInfo = new InvokeInfo(overridenCtx, handler, RouteType.INTERNAL_SERVER_ERROR, newLoadedController, null);
		
		return super.invokeErrorController(newInvokeInfo, newInfo, data);
	}

	/**
	 * This one is definitely special
	 */
	@Override
	public CompletableFuture<StreamWriter> invokeNotFound(InvokeInfo invokeInfo, Endpoint info, RouteData data) {
		//special case for if stuff didn't compile and we flag it
		Throwable exc = (Throwable) invokeInfo.getRequestCtx().getRequest().requestState.get(ERROR_KEY);
		if(exc != null) {
			log.error("Could not compile your code", exc);
			RouteInfoForInternalError error = new RouteInfoForInternalError(exc); 
			return invokeErrorController(invokeInfo, info, error);
		} 
		
		RequestContext requestCtx = invokeInfo.getRequestCtx();
		ProxyStreamHandle handler = invokeInfo.getHandler();
		RouteInfoForNotFound notFoundData = (RouteInfoForNotFound) data;
		NotFoundException notFoundExc = notFoundData.getNotFoundException();
		RouterRequest req = requestCtx.getRequest();
		
		if(notFoundData.getNotFoundException() == null) {
			throw new IllegalArgumentException("must have not found exception to be here");
		} else if(req.queryParams.containsKey(DevelopmentController.NOT_FOUND_KEY)) {
			//This is a callback so render the original webapp developer's not found page into the iframe
			return super.invokeNotFound(invokeInfo, info, data);
		}
		
		//ok, in dev mode, we hijack the not found page with one with a route list AND an iframe containing the developers original
		//notfound page
		
		log.error("(Development only log message) Route not found!!! Either you(developer) typed the wrong url OR you have a bad route.  Either way,\n"
				+ " something needs a'fixin.  req="+req, notFoundExc);

		Injector webAppInjector = webInjector.getCurrentInjector();

		RouteInfo routeInfo = new RouteInfo(new RouteModuleInfo("", null), "/org/webpieces/devrouter/impl/DevelopmentController.notFound");
		
		SvcProxyFixedRoutes svcProxy = new SvcProxyFixedRoutes(serviceInvoker, futureUtil);
		LoadedController newLoadedController = controllerFinder.loadGenericController(webAppInjector, routeInfo).getLoadedController();
		Endpoint newInfo = new Endpoint(svcProxy);
		
		String reason = "Your route was not found in routes table";
		if(notFoundExc != null)
			reason = notFoundExc.getMessage();
		
		RouterRequest newRequest = new RouterRequest();
		newRequest.putMultipart("webpiecesError", "Exception message="+reason);
		newRequest.putMultipart("url", req.relativePath);
		newRequest.isHttps = req.isHttps;
		newRequest.isBackendRequest = req.isBackendRequest;
		newRequest.originalRequest = req.originalRequest;
		newRequest.requestState.put(DevelopmentController.ORIGINAL_REQUEST, req);
		
		ApplicationContext ctx = webInjector.getAppContext();
		RequestContext overridenCtx = new RequestContext(requestCtx.getValidation(), (FlashSub) requestCtx.getFlash(), requestCtx.getSession(), newRequest, ctx);
		InvokeInfo newInvokeInfo = new InvokeInfo(overridenCtx, handler, RouteType.NOT_FOUND, newLoadedController, null);
		return super.invokeNotFound(newInvokeInfo, newInfo, data);
	}
}
