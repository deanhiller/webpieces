package org.webpieces.plugin.secure.properties;

import static org.webpieces.router.api.routes.Port.HTTPS;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.plugin.backend.BackendRoutes;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routebldr.ScopedRouteBuilder;

public class PropertiesRoutes extends BackendRoutes {
	
	@Override
	public void configure(RouteBuilder baseBldr, ScopedRouteBuilder scopedRouter1) {
		ScopedRouteBuilder scoped = baseBldr.getScopedRouteBuilder("/@properties");
		scoped.addRoute(HTTPS, HttpMethod.GET,  "", "PropertiesController.main", PropertiesRouteId.MAIN_PROPERTIES);
		scoped.addRoute(HTTPS, HttpMethod.GET, "/bean/{category}/{name}", "PropertiesController.bean", PropertiesRouteId.BEAN_ROUTE);
		scoped.addRoute(HTTPS, HttpMethod.POST, "/bean/{category}/{name}", "PropertiesController.postBean", PropertiesRouteId.POST_BEAN_CHANGES);
    }

}
