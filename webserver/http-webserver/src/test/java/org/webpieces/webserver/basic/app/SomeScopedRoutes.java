package org.webpieces.webserver.basic.app;

import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routebldr.ScopedRouteBuilder;
import org.webpieces.router.api.routes.ScopedRoutes;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.router.api.routes.Port.BOTH;

public class SomeScopedRoutes extends ScopedRoutes {

	@Override
	protected String getScope() {
		return "/scoped";
	}

	@Override
	protected void configure(RouteBuilder baseBldr, ScopedRouteBuilder scopedBldr) {
		//special corner case outside regex that we allow so /scope will match
		scopedBldr.addRoute(BOTH, GET , "",         "biz/BasicController.myMethod", BasicRouteId.SCOPED_ROOT);
		//special case matching /scope/
		scopedBldr.addRoute(BOTH, GET , "/",        "biz/BasicController.myMethod", BasicRouteId.SCOPED_ROOT2);
	}

}
