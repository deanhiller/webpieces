package org.webpieces.plugins.hsqldb;

import static org.webpieces.router.api.routing.Port.BOTH;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.routing.Routes;
import org.webpieces.router.impl.model.bldr.DomainRouteBuilder;
import org.webpieces.router.impl.model.bldr.RouteBuilder;

public class H2DbRoutes implements Routes {
	
	private String urlPath;

	public H2DbRoutes(H2DbConfig config) {
		urlPath = config.getPluginPath();
	}

	@Override
	public void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder router = domainRouteBldr.getAllDomainsRouteBuilder();
		router.addRoute(BOTH, HttpMethod.GET, urlPath, "H2DbController.databaseGui", H2DbRouteId.GET_DATABASE_PAGE);
	}

}