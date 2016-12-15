package org.webpieces.webserver.dev.app;

import static org.webpieces.ctx.api.HttpMethod.GET;

import org.webpieces.router.api.routing.AbstractRouteModule;

public class DevRouteModule extends AbstractRouteModule {

	@Override
	public void configure() {
		addRoute(GET , "/home",               "DevController.home", DevRouteId.HOME);
		
		addRoute(GET , "/newroute",           "DevController.existingRoute", DevRouteId.EXISTING);

		setPageNotFoundRoute("DevController.notFound");
		setInternalErrorRoute("DevController.internalError");
	}

}
