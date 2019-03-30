package org.webpieces.plugins.properties;

import static org.webpieces.router.api.routing.Port.HTTPS;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.plugins.backend.BackendRoutes;
import org.webpieces.router.impl.model.bldr.RouteBuilder;
import org.webpieces.router.impl.model.bldr.ScopedRouteBuilder;

public class PropertiesRoutes extends BackendRoutes {
	
	@Override
	public void configure(RouteBuilder baseBldr, ScopedRouteBuilder scopedRouter1) {
		ScopedRouteBuilder scopedRouter = scopedRouter1.getScopedRouteBuilder("/secure");
		scopedRouter.addRoute(HTTPS, HttpMethod.GET,  "/properties", "PropertiesController.main", PropertiesRouteId.MAIN_PROPERTIES);
		scopedRouter.addRoute(HTTPS, HttpMethod.GET, "/bean/{category}/{name}", "PropertiesController.bean", PropertiesRouteId.BEAN_ROUTE);
		scopedRouter.addRoute(HTTPS, HttpMethod.POST, "/bean/{category}/{name}", "PropertiesController.postBean", PropertiesRouteId.POST_BEAN_CHANGES);
    }

}
