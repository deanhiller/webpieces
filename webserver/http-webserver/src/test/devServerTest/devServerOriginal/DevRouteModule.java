package org.webpieces.webserver.dev.app;

import static org.webpieces.ctx.api.HttpMethod.GET;

import org.webpieces.router.api.routing.AbstractRouteModule;
import org.webpieces.router.api.routing.PortType;

public class DevRouteModule extends AbstractRouteModule {

	@Override
	public void configure(String currentPackage) {
		addRoute(GET , "/home",               "DevController.home", DevRouteId.HOME);
		
		addRoute(GET , "/causeError",         "DevController.causeError", DevRouteId.CAUSE_ERROR);

		addRoute(GET , "/filter",             "DevController.filter", DevRouteId.FILTER_ROUTE);
		
		addFilter("/filter", MyFilter.class, null, PortType.ALL_FILTER);
		
		addNotFoundFilter(NotFoundFilter.class, null, PortType.ALL_FILTER);
		
		setPageNotFoundRoute("DevController.notFound");
		setInternalErrorRoute("DevController.internalError");
	}

}
