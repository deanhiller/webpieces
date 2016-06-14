package org.webpieces.router.api.error.dev;

import static org.webpieces.router.api.dto.HttpMethod.GET;
import static org.webpieces.router.api.simplesvr.MtgRouteId.SOME_EXAMPLE;

import org.webpieces.router.api.routing.RouteModule;
import org.webpieces.router.api.routing.Router;

public class TooManyArgsRouteModule implements RouteModule {
	
	@Override
	public void configure(Router router, String packageName) {
		//We cannot do this or the compiler in dev router will compile it too early for testing
		//String controllerName = SomeController.class.getName();

		router.addRoute(GET, "/something",  "org.webpieces.devrouter.api.SomeController.argsMismatch", SOME_EXAMPLE);
		
		//router.addRoute(POST,     "/{controller}/{action}", "{controller}.post{action}", null);
		
		router.setCatchAllRoute("org.webpieces.devrouter.api.SomeController.notFound");
	}
}
