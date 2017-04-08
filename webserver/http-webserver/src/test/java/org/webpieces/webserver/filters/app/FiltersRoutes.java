package org.webpieces.webserver.filters.app;

import static org.webpieces.ctx.api.HttpMethod.GET;

import org.webpieces.router.api.routing.AbstractRoutes;
import org.webpieces.router.api.routing.PortType;

public class FiltersRoutes extends AbstractRoutes {

	@Override
	public void configure() {
		
		addRoute(GET , "/test/something",      "FiltersController.home", FiltersRouteId.HOME);

		//Unlike routes which apply regex to request urls, filters regexs are applied to route regexs so if a filter
		//matches a route, it will be added to all requests for that route.  This is done so we don't have to
		//figure out which filters to apply on each request and on startup can wire up all filters once
		addFilter("/test/.*", StatefulFilter.class, 1, PortType.ALL_FILTER);
		addFilter("/test/.*", StatefulFilter.class, 2, PortType.ALL_FILTER);
		
		setPageNotFoundRoute("/org/webpieces/webserver/basic/app/biz/BasicController.notFound");
		setInternalErrorRoute("/org/webpieces/webserver/basic/app/biz/BasicController.internalError");
	}

}
