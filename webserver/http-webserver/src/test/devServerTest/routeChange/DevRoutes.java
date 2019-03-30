package org.webpieces.webserver.dev.app;

import static org.webpieces.ctx.api.HttpMethod.GET;

import org.webpieces.router.api.routing.Routes;
import org.webpieces.router.impl.model.bldr.DomainRouteBuilder;
import org.webpieces.router.impl.model.bldr.RouteBuilder;

import static org.webpieces.router.api.routing.Port.BOTH;

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
