package org.webpieces.devrouter.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.Actions;
import org.webpieces.router.api.routes.Port;
import org.webpieces.router.impl.Route;
import org.webpieces.router.impl.RouteMeta;
import org.webpieces.router.impl.RoutingHolder;
import org.webpieces.router.impl.routing.DomainRouter;
import org.webpieces.router.impl.routing.Router;
import org.webpieces.router.impl.routing.ScopedRouter;

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
		
		DomainRouter domainRouter = routingHolder.getDomainRouter();
		Router router = domainRouter.getLeftOverDomains();
		
		Collection<Router> routers = new ArrayList<>();
		for(Router oneRouter : domainRouter.getDomainToRouter().values()) {
			routers.add(oneRouter);
		}
		
		//This is a pain but dynamically build up the html
		String routeHtml = build(router);
		
		return Actions.renderThis("domains", routers, "routeHtml", routeHtml, "error", error, "url", url);
	}

	private String build(ScopedRouter mainRoutes) {
		String html = "<ul>\n";
		
		Map<String, ScopedRouter> scopedRoutes = mainRoutes.getPathPrefixToNextRouter();
		for(Map.Entry<String, ScopedRouter> entry : scopedRoutes.entrySet()) {
			html += "<li>"+ "SCOPE:"+entry.getKey()+"</li>";
			html += build(entry.getValue());
		}
		
		List<RouteMeta> routes = mainRoutes.getRoutes();
		for(RouteMeta route: routes) {
			Route rt = route.getRoute();
			boolean isHttpsOnly = rt.getExposedPorts() == Port.HTTPS;
			String http = isHttpsOnly ? "https" : "http";
			html += "<li>"+pad(rt.getMethod(), 5)+":"+pad(http, 5)+" : "+rt.getFullPath()+"</li>\n";
		}
		
		html+="</ul>\n";
		
		return html;
	}

	private String pad(String msg, int n) {
		int left = n-msg.length();
		if(left < 0)
			left = 0;
		
		for(int i = 0; i < left; i++) {
			msg += "&nbsp;";
		}
		return msg;
	}
}
