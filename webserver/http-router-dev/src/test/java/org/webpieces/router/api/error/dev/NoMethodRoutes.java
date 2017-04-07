package org.webpieces.router.api.error.dev;

import static org.webpieces.router.api.simplesvr.MtgRouteId.SOME_EXAMPLE;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.routing.Router;
import org.webpieces.router.api.routing.Routes;

public class NoMethodRoutes implements Routes {
	
	@Override
	public void configure(Router router) {
		//We cannot do this or the compiler in dev router will compile it too early for testing
		//String controllerName = SomeController.class.getName();

		router.addRoute(HttpMethod.GET, "/something",  "org.webpieces.devrouter.api.CommonController.thisMethodNotExist", SOME_EXAMPLE);
		
		//router.addRoute(POST,     "/{controller}/{action}", "{controller}.post{action}", null);
		
		router.setPageNotFoundRoute("MeetingController.notFound");
		router.setInternalErrorRoute("MeetingController.internalError");
	}

}
