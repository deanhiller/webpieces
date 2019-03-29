package org.webpieces.webserver.dev.app;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.router.api.routing.Port.BOTH;

import org.webpieces.router.api.routing.PortType;
import org.webpieces.router.api.routing.Routes;
import org.webpieces.router.impl.model.bldr.DomainRouteBuilder;
import org.webpieces.router.impl.model.bldr.RouteBuilder;

public class DevRouteModule implements Routes {

	@Override
	public void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder router = domainRouteBldr.getAllDomainsRouteBuilder();
		router.addRoute(BOTH, GET , "/home",               "DevController.home", DevRouteId.HOME);
		
		router.addRoute(BOTH, GET , "/causeError",         "DevController.causeError", DevRouteId.CAUSE_ERROR);

		router.addRoute(BOTH, GET , "/filter",             "DevController.filter", DevRouteId.FILTER_ROUTE);
		
		router.addFilter("/filter", MyFilter.class, null, PortType.ALL_FILTER);
		
		router.addNotFoundFilter(NotFoundFilter.class, null, PortType.ALL_FILTER);
		
		router.setPageNotFoundRoute("DevController.notFound");
		router.setInternalErrorRoute("DevController.internalError");
	}

}
