package org.webpieces.webserver.routing.app;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.router.api.routes.Port.BOTH;

import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routes.BasicRoutes;

public class Domain1Routes implements BasicRoutes {

	@Override
	public void configure(RouteBuilder bldr) {
		bldr.addRoute(BOTH, GET ,     "/domain1",             "DomainsController.domain1", DomainsRouteId.DOMAIN1);
		
		bldr.addStaticFile(BOTH, "/public1/myfile", "src/test/resources/tagsMeta.txt", false);
		//relative path(relative to baseDirectory in RouterConfig)
		bldr.addStaticDir(BOTH, "/public1/", "src/test/resources/staticRoutes/", false);
		
		bldr.setPageNotFoundRoute("DomainsController.notFoundDomain1");
		bldr.setInternalErrorRoute("/org/webpieces/webserver/basic/app/biz/BasicController.internalError");
	}

}
