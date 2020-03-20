package org.webpieces.webserver.dev.app;

import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routes.FilterPortType;
import org.webpieces.router.api.routes.Routes;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.router.api.routes.Port.BOTH;

public class DevRouteModule implements Routes {

	@Override
	public void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder bldr = domainRouteBldr.getAllDomainsRouteBuilder();
		bldr.addRoute(BOTH, GET , "/home",               "DevController.home", DevRouteId.HOME);
		
		bldr.addRoute(BOTH, GET , "/causeError",         "DevController.causeError", DevRouteId.CAUSE_ERROR);

		bldr.addRoute(BOTH, GET , "/filter",             "DevController.filter", DevRouteId.FILTER_ROUTE);
		
		bldr.addFilter("/filter", MyFilter.class, null, FilterPortType.ALL_FILTER);
		
		bldr.addNotFoundFilter(NotFoundFilter.class, null, FilterPortType.ALL_FILTER);
		
		bldr.setPageNotFoundRoute("DevController.notFound");
		bldr.setInternalErrorRoute("DevController.internalError");
	}

}
