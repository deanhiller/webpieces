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
		RouteBuilder bldr = domainRouteBldr.getAllDomainsRouteBuilder();
		bldr.addRoute(BOTH, GET , "/home",               "DevController.home", DevRouteId.HOME);
		
		bldr.addRoute(BOTH, GET , "/causeError",         "DevController.causeError", DevRouteId.CAUSE_ERROR);

		bldr.addRoute(BOTH, GET , "/filter",             "DevController.filter", DevRouteId.FILTER_ROUTE);
		
		bldr.addFilter("/filter", MyFilter.class, null, PortType.ALL_FILTER);
		
		bldr.addNotFoundFilter(NotFoundFilter.class, null, PortType.ALL_FILTER);
		
		bldr.setPageNotFoundRoute("DevController.notFound");
		bldr.setInternalErrorRoute("DevController.internalError");
	}

}
