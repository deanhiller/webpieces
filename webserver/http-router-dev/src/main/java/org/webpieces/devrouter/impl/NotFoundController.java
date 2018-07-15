package org.webpieces.devrouter.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;
import org.webpieces.router.impl.Route;
import org.webpieces.router.impl.RouteMeta;
import org.webpieces.router.impl.RoutingHolder;
import org.webpieces.router.impl.model.L1AllRouting;
import org.webpieces.router.impl.model.L2DomainRoutes;
import org.webpieces.router.impl.model.L3PrefixedRouting;
import org.webpieces.router.impl.model.R1RouterBuilder;

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
		
		R1RouterBuilder builder = routingHolder.getRouterBuilder();
		L1AllRouting routingInfo = builder.getRouterInfo();
		Collection<L2DomainRoutes> domains = routingInfo.getSpecificDomains();

		L3PrefixedRouting mainRoutes = routingInfo.getMainRoutes().getRoutesForDomain();
		//This is a pain but dynamically build up the html
		String routeHtml = build(mainRoutes);
		
		return Actions.renderThis("domains", domains, "routeHtml", routeHtml, "error", error, "url", url);
	}

	private String build(L3PrefixedRouting mainRoutes) {
		String html = "<ul>\n";
		
		Map<String, L3PrefixedRouting> scopedRoutes = mainRoutes.getScopedRoutes();
		for(Map.Entry<String, L3PrefixedRouting> entry : scopedRoutes.entrySet()) {
			html += "<li>"+ "SCOPE:"+entry.getKey()+"</li>";
			html += build(entry.getValue());
		}
		
		List<RouteMeta> routes = mainRoutes.getRoutes();
		for(RouteMeta route: routes) {
			Route rt = route.getRoute();
			String http = rt.isHttpsRoute() ? "https" : "http";
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
