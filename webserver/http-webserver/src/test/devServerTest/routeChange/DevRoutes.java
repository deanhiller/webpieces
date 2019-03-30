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
		RouteBuilder bldr = domainBldr.getAllDomainsRouteBuilder();
		bldr.addRoute(BOTH, GET , "/home",               "DevController.home", DevRouteId.HOME);
		
		bldr.addRoute(BOTH, GET , "/newroute",           "DevController.existingRoute", DevRouteId.EXISTING);

		bldr.setPageNotFoundRoute("DevController.notFound");
		bldr.setInternalErrorRoute("DevController.internalError");
	}

}
