package org.webpieces.plugins.hsqldb;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.routing.AbstractRoutes;

public class H2DbRoutes extends AbstractRoutes {
	
	private String urlPath;

	public H2DbRoutes(H2DbConfig config) {
		urlPath = config.getUrlPath();
	}

	@Override
	protected void configure() {
		addRoute(HttpMethod.GET, urlPath, "H2DbController.databaseGui", H2DbRouteId.GET_DATABASE_PAGE);
	}

}