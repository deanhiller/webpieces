package org.webpieces.plugins.hsqldb;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.routing.AbstractRoutes;

public class H2DbRoutes extends AbstractRoutes {
	
	@Override
	protected void configure() {
		addRoute(HttpMethod.GET, "/@db", "H2DbController.databaseGui", H2DbRouteId.GET_DATABASE_PAGE);
	}

}