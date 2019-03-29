package org.webpieces.plugins.fortesting;

import org.webpieces.router.api.routing.Routes;
import org.webpieces.router.impl.model.bldr.DomainRouteBuilder;
import org.webpieces.router.impl.model.bldr.RouteBuilder;

public class FillerRoutes implements Routes {

	@Override
	public void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder bldr = domainRouteBldr.getAllDomainsRouteBuilder();
		bldr.setPageNotFoundRoute("/org/webpieces/plugins/fortesting/BasicController.notFound");
		bldr.setInternalErrorRoute("/org/webpieces/plugins/fortesting/BasicController.internalError");
	}

}
