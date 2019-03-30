package org.webpieces.router.api.error.dev;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.router.api.routes.Port.BOTH;
import static org.webpieces.router.api.simplesvr.MtgRouteId.ARGS_MISMATCH;
import static org.webpieces.router.api.simplesvr.MtgRouteId.SOME_EXAMPLE;

import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routes.Routes;

public class CommonRoutes implements Routes {

	@Override
	public void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder bldr = domainRouteBldr.getAllDomainsRouteBuilder();
		//We cannot do this or the compiler in dev router will compile it too early for testing
		//String controllerName = SomeController.class.getName();

		bldr.addRoute(BOTH, GET, "/user/{id}",  "org.webpieces.devrouter.api.CommonController.badRedirect", SOME_EXAMPLE);
		bldr.addRoute(BOTH, GET, "/something",  "org.webpieces.devrouter.api.CommonController.argsMismatch", ARGS_MISMATCH);
		
		//bldr.addRoute(BOTH, POST,     "/{controller}/{action}", "{controller}.post{action}", null);
		
		bldr.setPageNotFoundRoute("org.webpieces.devrouter.api.CommonController.notFound");
		bldr.setInternalErrorRoute("org.webpieces.devrouter.api.CommonController.internalError");
	}

}
