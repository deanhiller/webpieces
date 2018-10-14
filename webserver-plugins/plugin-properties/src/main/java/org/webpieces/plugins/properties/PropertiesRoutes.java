package org.webpieces.plugins.properties;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.plugins.backend.BackendRoutes;
import org.webpieces.router.api.routing.Router;

public class PropertiesRoutes extends BackendRoutes {
	
	@Override
	protected void configure() {
		Router https = getScopedRouter("/secure", true);
		https.addRoute(HttpMethod.GET,  "/properties", "PropertiesController.main", PropertiesRouteId.MAIN_PROPERTIES);
		https.addRoute(HttpMethod.GET, "/bean/{category}/{name}", "PropertiesController.bean", PropertiesRouteId.BEAN_ROUTE);
		https.addRoute(HttpMethod.POST, "/bean/{category}/{name}", "PropertiesController.postBean", PropertiesRouteId.POST_BEAN_CHANGES);
    }

}
