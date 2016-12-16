package org.webpieces.plugins.hsqldb;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.routing.AbstractRouteModule;

public class H2DbRouteModule extends AbstractRouteModule {
	
	@Override
	protected void configure() {
		addRoute(HttpMethod.GET, "/@db", "H2DbController.databaseGui", H2DbRouteId.GET_DATABASE_PAGE);
	}

}