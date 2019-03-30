package org.webpieces.webserver.dev.app;

import static org.webpieces.ctx.api.HttpMethod.GET;

import org.webpieces.router.api.routing.Router;
import org.webpieces.router.api.routing.Routes;
import org.webpieces.router.impl.model.bldr.DomainRouteBuilder;
import org.webpieces.router.impl.model.bldr.RouteBuilder;

import static org.webpieces.router.api.routing.Port.BOTH;

public class DevRoutes implements Routes {

	@Override
	public void configure(DomainRouteBuilder domainBldr) {
		RouteBuilder router = domainBldr.getAllDomainsRouteBuilder();
		router.addRoute(BOTH, GET , "/home",               "DevController.home", DevRouteId.HOME);
		
		router.addRoute(BOTH, GET , "/newroute",           "DevController.existingRoute", DevRouteId.EXISTING);

		router.setPageNotFoundRoute("DevController.notFound");
		router.setInternalErrorRoute("DevController.internalError");
	}

}
