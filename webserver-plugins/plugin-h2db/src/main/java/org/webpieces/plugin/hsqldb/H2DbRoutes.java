package org.webpieces.plugin.hsqldb;

import static org.webpieces.router.api.routes.Port.BOTH;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routes.Routes;

public class H2DbRoutes implements Routes {
	
	private String urlPath;

	public H2DbRoutes(H2DbConfig config) {
		urlPath = config.getPluginPath();
	}

	@Override
	public void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder bldr = domainRouteBldr.getBackendBuilder().getBldrForAllOtherContentTypes();
		bldr.addRoute(BOTH, HttpMethod.GET, urlPath, "H2DbController.redirectToDatabaseGui", H2DbRouteId.REDIRECT_TO_DB_GUI);
		
		RouteBuilder httpBuilder = domainRouteBldr.getAllDomainsRouteBuilder();
		httpBuilder.addRoute(BOTH, HttpMethod.GET, urlPath+"/database", "H2DbController.databaseGui", H2DbRouteId.DATABASE_GUI_PAGE);
	}

}