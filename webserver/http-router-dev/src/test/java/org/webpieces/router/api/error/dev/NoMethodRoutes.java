package org.webpieces.router.api.error.dev;

import static org.webpieces.ctx.api.HttpMethod.getAll;
import static org.webpieces.router.api.simplesvr.MtgRouteId.SOME_EXAMPLE;

import org.webpieces.router.api.routing.Routes;
import org.webpieces.router.api.routing.Router;

public class NoMethodRoutes implements Routes {
	
	@Override
	public void configure(Router router) {
		//We cannot do this or the compiler in dev router will compile it too early for testing
		//String controllerName = SomeController.class.getName();

		router.addRoute(getAll(), "/something",  "org.webpieces.devrouter.api.CommonController.thisMethodNotExist", SOME_EXAMPLE);
		
		//router.addRoute(POST,     "/{controller}/{action}", "{controller}.post{action}", null);
		
		router.setPageNotFoundRoute("MeetingController.notFound");
		router.setInternalErrorRoute("MeetingController.internalError");
	}

}
