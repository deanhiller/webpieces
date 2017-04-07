package org.webpieces.webserver.domains.app;

import static org.webpieces.ctx.api.HttpMethod.GET;

import org.webpieces.router.api.routing.AbstractRoutes;
import org.webpieces.router.api.routing.Routes;

public class Domain1Routes extends AbstractRoutes implements Routes {

	@Override
	protected void configure() {
		addRoute(GET ,     "/domain1",             "DomainsController.domain1", DomainsRouteId.DOMAIN1);
		
		setPageNotFoundRoute("DomainsController.notFoundDomain1");
		setInternalErrorRoute("/org/webpieces/webserver/basic/app/biz/BasicController.internalError");
	}

}
