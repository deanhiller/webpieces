package org.webpieces.webserver.basic.app;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.router.api.routing.Port.BOTH;

import org.webpieces.router.api.routing.ScopedRoutes;
import org.webpieces.router.impl.model.bldr.RouteBuilder;
import org.webpieces.router.impl.model.bldr.ScopedRouteBuilder;

public class SomeScopedRoutes extends ScopedRoutes {

	@Override
	protected String getScope() {
		return "/scoped";
	}

	@Override
	protected void configure(RouteBuilder baseRouter, ScopedRouteBuilder scopedRouter) {
		//special corner case outside regex that we allow so /scope will match
		scopedRouter.addRoute(BOTH, GET , "",         "biz/BasicController.myMethod", BasicRouteId.SCOPED_ROOT);
		//special case matching /scope/
		scopedRouter.addRoute(BOTH, GET , "/",        "biz/BasicController.myMethod", BasicRouteId.SCOPED_ROOT2);
	}

}
