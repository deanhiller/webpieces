package org.webpieces.webserver.domains.app;

import static org.webpieces.ctx.api.HttpMethod.GET;

import org.webpieces.router.api.routing.AbstractRouteModule;
import org.webpieces.router.api.routing.RouteModule;

public class Domain1Module extends AbstractRouteModule implements RouteModule {

	@Override
	protected void configure() {
		addRoute(GET ,     "/domain1",             "DomainsController.domain1", DomainsRouteId.DOMAIN1);
	}

}
