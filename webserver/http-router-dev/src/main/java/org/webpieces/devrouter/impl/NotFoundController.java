package org.webpieces.devrouter.impl;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.Actions;
import org.webpieces.router.impl.RoutingHolder;
import org.webpieces.router.impl.routers.BDomainRouter;
import org.webpieces.router.impl.routers.CRouter;
import org.webpieces.router.impl.routers.DScopedRouter;

@Singleton
public class NotFoundController {

	@Inject
	private RoutingHolder routingHolder;
	
	public Action notFound() {
		RouterRequest request = Current.request();
		String error = request.getSingleMultipart("webpiecesError");
		String url = request.getSingleMultipart("url");
		
		if(url.contains("?")) {
			url += "&webpiecesShowPage=true";
		} else {
			url += "?webpiecesShowPage=true";
		}
		
		BDomainRouter domainRouter = routingHolder.getDomainRouter();
		CRouter router = domainRouter.getLeftOverDomains();
		
		Collection<CRouter> routers = new ArrayList<>();
		for(CRouter oneRouter : domainRouter.getDomainToRouter().values()) {
			routers.add(oneRouter);
		}
		
		//This is a pain but dynamically build up the html
		String routeHtml = build(router);
		
		return Actions.renderThis("domains", routers, "routeHtml", routeHtml, "error", error, "url", url);
	}

	private String build(DScopedRouter mainRoutes) {
		return mainRoutes.buildHtml(" ");
	}
}
