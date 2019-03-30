package org.webpieces.devrouter.impl;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.dto.MethodMeta;
import org.webpieces.router.api.dto.RouteType;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.impl.NotFoundInfo;
import org.webpieces.router.impl.RouteImpl;
import org.webpieces.router.impl.RouteInvoker2;
import org.webpieces.router.impl.RouteMeta;
import org.webpieces.router.impl.loader.ControllerLoader;
import org.webpieces.router.impl.model.MatchResult;
import org.webpieces.router.impl.model.RouteModuleInfo;
import org.webpieces.router.impl.model.bldr.data.DomainRouter;
import org.webpieces.router.impl.params.ObjectToParamTranslator;
import org.webpieces.util.filters.Service;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public class DevRouteInvoker extends RouteInvoker2 {
	private static final Logger log = LoggerFactory.getLogger(DevRouteInvoker.class);
	private ServiceCreator serviceCreator;

	@Inject
	public DevRouteInvoker(ObjectToParamTranslator reverseTranslator, RouterConfig config, ControllerLoader loader, ServiceCreator creator) {
		super(reverseTranslator, config, loader);
		this.serviceCreator = creator;
	}

	@Override
	public CompletableFuture<Void> invokeController(MatchResult result, RequestContext requestCtx,
			ResponseStreamer responseCb, NotFoundException notFoundExc) {
		
		RouteMeta meta = result.getMeta();
		controllerFinder.loadControllerIntoMetaObject(meta, false);
		controllerFinder.loadFiltersIntoMeta(meta, false);
		
		if(notFoundExc != null) {
			return invokeCorrectNotFoundRoute(result, requestCtx, responseCb, notFoundExc);
		}
		
		return super.invokeController(result, requestCtx, responseCb, notFoundExc);
	}

	public CompletableFuture<Void> invokeCorrectNotFoundRoute(MatchResult result, RequestContext requestCtx,
			ResponseStreamer responseCb, NotFoundException notFoundExc) {
		RouterRequest req = requestCtx.getRequest();
		//RouteMeta origMeta, NotFoundException e, RouterRequest req) {
		if(req.queryParams.containsKey("webpiecesShowPage")) {
			//This is a callback so render the original webapp developer's not found page into the iframe
			return super.invokeController(result, requestCtx, responseCb, notFoundExc);
		}

		//ok, in dev mode, we hijack the not found page with one with a route list AND an iframe containing the developers original
		//notfound page
		
		log.error("(Development only log message) Route not found!!! Either you(developer) typed the wrong url OR you have a bad route.  Either way,\n"
				+ " something needs a'fixin.  req="+req, notFoundExc);
		
		RouteMeta origMeta = result.getMeta();
		RouteImpl r = new RouteImpl(this, "/org/webpieces/devrouter/impl/NotFoundController.notFound", RouteType.NOT_FOUND);
		RouteModuleInfo info = new RouteModuleInfo("", null);
		RouteMeta meta = new RouteMeta(r, origMeta.getInjector(), info, config.getUrlEncoding());
		
		if(meta.getControllerInstance() == null) {
			controllerFinder.loadControllerIntoMetaObject(meta, false);
			meta.setService(serviceCreator.create());
		}
		
		String reason = "Your route was not found in routes table";
		if(notFoundExc != null)
			reason = notFoundExc.getMessage();
		
		RouterRequest newRequest = new RouterRequest();
		newRequest.putMultipart("webpiecesError", "Exception message="+reason);
		newRequest.putMultipart("url", req.relativePath);
		
		super.invokeController(new , requestCtx, responseCb, notFoundExc)
		return new NotFoundInfo(meta, meta.getService222(), newRequest);
	}
}
