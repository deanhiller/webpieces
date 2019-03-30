package org.webpieces.router.api.error.dev;

import static org.webpieces.router.api.routes.Port.BOTH;
import static org.webpieces.router.api.simplesvr.MtgRouteId.SOME_EXAMPLE;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routes.Routes;

public class NoMethodRoutes implements Routes {
	
	@Override
	public void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder bldr = domainRouteBldr.getAllDomainsRouteBuilder();
		//We cannot do this or the compiler in dev router will compile it too early for testing
		//String controllerName = SomeController.class.getName();

		bldr.addRoute(BOTH, HttpMethod.GET, "/something",  "org.webpieces.devrouter.api.CommonController.thisMethodNotExist", SOME_EXAMPLE);
		
		//bldr.addRoute(false, POST,     "/{controller}/{action}", "{controller}.post{action}", null);
		
		bldr.setPageNotFoundRoute("ErrorController.notFound");
		bldr.setInternalErrorRoute("ErrorController.internalError");
	}

}
