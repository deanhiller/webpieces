package org.webpieces.webserver.dev.app;

import static org.webpieces.ctx.api.HttpMethod.GET;

import org.webpieces.router.api.routing.Router;
import org.webpieces.router.api.routing.Routes;
import static org.webpieces.router.api.routing.Port.BOTH;

public class DevRoutes implements Routes {

	@Override
	public void configure(Router router) {
		router.addRoute(BOTH, GET , "/home",               "DevController.home", DevRouteId.HOME);
		
		router.addRoute(BOTH, GET , "/causeError",         "DevController.causeError", DevRouteId.CAUSE_ERROR);

		router.setPageNotFoundRoute("DevController.notFound");
		router.setInternalErrorRoute("DevController.internalError");
	}

}
