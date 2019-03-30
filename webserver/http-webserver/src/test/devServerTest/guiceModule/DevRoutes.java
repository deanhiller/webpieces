package org.webpieces.webserver.dev.app;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.router.api.routes.Port.BOTH;

import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routes.FilterPortType;
import org.webpieces.router.api.routes.Routes;

public class DevRoutes implements Routes {

	@Override
	public void configure(DomainRouteBuilder domainBldr) {
		RouteBuilder router = domainBldr.getAllDomainsRouteBuilder();
		router.addRoute(BOTH, GET , "/home",               "DevController.home", DevRouteId.HOME);
		
		router.addRoute(BOTH, GET , "/causeError",         "DevController.causeError", DevRouteId.CAUSE_ERROR);

		router.setPageNotFoundRoute("DevController.notFound");
		router.setInternalErrorRoute("DevController.internalError");
	}

}
