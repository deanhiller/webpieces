package org.webpieces.plugins.fortesting;

import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routes.Routes;

public class FillerRoutes implements Routes {

	@Override
	public void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder bldr = domainRouteBldr.getAllDomainsRouteBuilder();
		bldr.setPageNotFoundRoute("/org/webpieces/plugins/fortesting/BasicController.notFound");
		bldr.setInternalErrorRoute("/org/webpieces/plugins/fortesting/BasicController.internalError");
	}

}
