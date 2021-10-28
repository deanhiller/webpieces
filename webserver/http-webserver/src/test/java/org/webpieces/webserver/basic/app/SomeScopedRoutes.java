package org.webpieces.webserver.basic.app;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.router.api.routes.Port.BOTH;

import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routebldr.ScopedRouteBuilder;
import org.webpieces.router.api.routes.ScopedRoutes;

public class SomeScopedRoutes extends ScopedRoutes {

	public String getScope() {
		return "/scoped";
	}

	public void configure(RouteBuilder baseBldr, ScopedRouteBuilder scopedBldr) {
		//special corner case outside regex that we allow so /scope will match
		scopedBldr.addRoute(BOTH, GET , "",         "biz/BasicController.myMethod", BasicRouteId.SCOPED_ROOT);
		//special case matching /scope/
		scopedBldr.addRoute(BOTH, GET , "/",        "biz/BasicController.myMethod", BasicRouteId.SCOPED_ROOT2);
	}

}
