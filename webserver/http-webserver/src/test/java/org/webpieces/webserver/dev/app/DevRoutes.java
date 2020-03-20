package org.webpieces.webserver.dev.app;

import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routes.FilterPortType;
import org.webpieces.router.api.routes.Routes;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.router.api.routes.Port.BOTH;

public class DevRoutes implements Routes {

	@Override
	public void configure(DomainRouteBuilder domainBldr) {
		RouteBuilder router = domainBldr.getAllDomainsRouteBuilder();

		router.addRoute(BOTH, GET , "/home",               "DevController.home", DevRouteId.HOME);
		
		router.addRoute(BOTH, GET , "/causeError",         "DevController.causeError", DevRouteId.CAUSE_ERROR);

		router.addRoute(BOTH, GET , "/filter",             "DevController.filter", DevRouteId.FILTER_ROUTE);
		
		router.addFilter("/filter", MyFilter.class, null, FilterPortType.ALL_FILTER);
		
		router.addNotFoundFilter(NotFoundFilter.class, null, FilterPortType.ALL_FILTER);
		
		router.setPageNotFoundRoute("DevController.notFound");
		router.setInternalErrorRoute("DevController.internalError");
	}

}
