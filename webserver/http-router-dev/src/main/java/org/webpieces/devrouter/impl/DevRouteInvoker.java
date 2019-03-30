package org.webpieces.devrouter.impl;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.impl.RouteInvoker2;
import org.webpieces.router.impl.RouteMeta;
import org.webpieces.router.impl.loader.ControllerLoader;
import org.webpieces.router.impl.model.MatchResult;
import org.webpieces.router.impl.params.ObjectToParamTranslator;

public class DevRouteInvoker extends RouteInvoker2 {

	@Inject
	public DevRouteInvoker(ObjectToParamTranslator reverseTranslator, RouterConfig config, ControllerLoader loader) {
		super(reverseTranslator, config, loader);
	}

	@Override
	public CompletableFuture<Void> invokeController(MatchResult result, RequestContext requestCtx,
			ResponseStreamer responseCb, boolean isNotFoundRoute) {
		
		RouteMeta meta = result.getMeta();
		controllerFinder.loadControllerIntoMetaObject(meta, false);
		controllerFinder.loadFiltersIntoMeta(meta, false);
		
		
		return super.invokeController(result, requestCtx, responseCb, isNotFoundRoute);
	}
	
}
