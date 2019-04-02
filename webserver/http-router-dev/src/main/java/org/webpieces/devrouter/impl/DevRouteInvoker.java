package org.webpieces.devrouter.impl;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.webpieces.ctx.api.FlashSub;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.impl.RouteImpl;
import org.webpieces.router.impl.RouteInvoker2;
import org.webpieces.router.impl.RouteMeta;
import org.webpieces.router.impl.dto.RouteType;
import org.webpieces.router.impl.loader.ControllerLoader;
import org.webpieces.router.impl.loader.svc.ServiceInvoker;
import org.webpieces.router.impl.loader.svc.SvcProxyFixedRoutes;
import org.webpieces.router.impl.model.MatchResult;
import org.webpieces.router.impl.model.RouteModuleInfo;
import org.webpieces.router.impl.params.ObjectToParamTranslator;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public class DevRouteInvoker extends RouteInvoker2 {
	private static final Logger log = LoggerFactory.getLogger(DevRouteInvoker.class);
	private final ServiceInvoker serviceInvoker;

	@Inject
	public DevRouteInvoker(ObjectToParamTranslator reverseTranslator, RouterConfig config, ControllerLoader loader, ServiceInvoker invoker) {
		super(reverseTranslator, config, loader);
		serviceInvoker = invoker;
	}

	@Override
	public CompletableFuture<Void> invokeNotFound(MatchResult result, RequestContext requestCtx, ResponseStreamer responseCb, NotFoundException notFoundExc) {
		RouteMeta meta = result.getMeta();
		if(meta.getControllerInstance() == null) {
			controllerFinder.loadControllerIntoMetaHtml(meta, false);
			meta.setService(new SvcProxyFixedRoutes(serviceInvoker));
		}
		
		if(notFoundExc == null) {
			throw new IllegalArgumentException("must have not found exception to be here");
		}
		
		return invokeCorrectNotFoundRoute(result, requestCtx, responseCb, notFoundExc);
	}
	
	@Override
	public CompletableFuture<Void> invokeErrorRoute(MatchResult result, RequestContext requestCtx, ResponseStreamer responseCb) {

		RouteMeta meta = result.getMeta();
		if(meta.getControllerInstance() == null) {
			controllerFinder.loadControllerIntoMetaHtml(meta, false);
			controllerFinder.loadFiltersIntoErrorMeta(meta, false);
		}
		return super.invokeErrorRoute(result, requestCtx, responseCb);

	}

	@Override
	public CompletableFuture<Void> invokeContentController(MatchResult result, RequestContext requestCtx, ResponseStreamer responseCb) {
		RouteMeta meta = result.getMeta();
		//If we haven't loaded it already, load it now
		if(meta.getControllerInstance() == null) {
			controllerFinder.loadControllerIntoMetaContent(meta, false);
			controllerFinder.loadFiltersIntoContentMeta(meta, false);
		}

		return super.invokeContentController(result, requestCtx, responseCb);
	}
	
	@Override
	public CompletableFuture<Void> invokeHtmlController(MatchResult result, RequestContext requestCtx, ResponseStreamer responseCb) {
		
		RouteMeta meta = result.getMeta();
		if(meta.getControllerInstance() == null) {
			controllerFinder.loadControllerIntoMetaHtml(meta, false);
			controllerFinder.loadFiltersIntoHtmlMeta(meta, false);
		}
		
		return super.invokeHtmlController(result, requestCtx, responseCb);
	}

	private CompletableFuture<Void> invokeCorrectNotFoundRoute(MatchResult result, RequestContext requestCtx,
			ResponseStreamer responseCb, NotFoundException notFoundExc) {
		RouterRequest req = requestCtx.getRequest();
		//RouteMeta origMeta, NotFoundException e, RouterRequest req) {
		if(req.queryParams.containsKey("webpiecesShowPage")) {
			//This is a callback so render the original webapp developer's not found page into the iframe
			return super.invokeNotFound(result, requestCtx, responseCb, notFoundExc);
		}

		//ok, in dev mode, we hijack the not found page with one with a route list AND an iframe containing the developers original
		//notfound page
		
		log.error("(Development only log message) Route not found!!! Either you(developer) typed the wrong url OR you have a bad route.  Either way,\n"
				+ " something needs a'fixin.  req="+req, notFoundExc);
		
		RouteMeta origMeta = result.getMeta();
		RouteImpl r = new RouteImpl(this, "/org/webpieces/devrouter/impl/NotFoundController.notFound", RouteType.NOT_FOUND);
		RouteModuleInfo info = new RouteModuleInfo("", null);
		RouteMeta meta = new RouteMeta(r, origMeta.getInjector(), controllerFinder, info, config.getUrlEncoding());
		
		if(meta.getControllerInstance() == null) {
			controllerFinder.loadControllerIntoMetaHtml(meta, false);
			meta.setService(new SvcProxyFixedRoutes(serviceInvoker));
		}
		
		String reason = "Your route was not found in routes table";
		if(notFoundExc != null)
			reason = notFoundExc.getMessage();
		
		RouterRequest newRequest = new RouterRequest();
		newRequest.putMultipart("webpiecesError", "Exception message="+reason);
		newRequest.putMultipart("url", req.relativePath);
		
		RequestContext overridenCtx = new RequestContext(requestCtx.getValidation(), (FlashSub) requestCtx.getFlash(), requestCtx.getSession(), newRequest);
		return super.invokeNotFound(new MatchResult(meta), overridenCtx, responseCb, notFoundExc);
	}
}
