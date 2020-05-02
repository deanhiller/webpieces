package org.webpieces.webserver.dev.app;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.router.api.routes.Port.BOTH;

import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routes.FilterPortType;
import org.webpieces.router.api.routes.Routes;

public class DevRouteModule implements Routes {

	@Override
	public void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder bldr = domainRouteBldr.getAllDomainsRouteBuilder();
		bldr.addRoute(BOTH, GET , "/home",               "DevController.home", DevRouteId.HOME);
		
		bldr.addRoute(BOTH, GET , "/causeError",         "DevController.causeError", DevRouteId.CAUSE_ERROR);

		bldr.addRoute(BOTH, GET , "/filter",             "DevController.filter", DevRouteId.FILTER_ROUTE);
		
		bldr.addFilter("/filter", MyFilter.class, null, FilterPortType.ALL_FILTER, 0);
		
		bldr.addNotFoundFilter(NotFoundFilter.class, null, FilterPortType.ALL_FILTER, 0);
		
		bldr.setPageNotFoundRoute("DevController.notFound");
		bldr.setInternalErrorRoute("DevController.internalError");
	}

}
