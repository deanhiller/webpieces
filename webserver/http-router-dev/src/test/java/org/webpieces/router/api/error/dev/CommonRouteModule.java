package org.webpieces.router.api.error.dev;

import static org.webpieces.router.api.dto.HttpMethod.GET;
import static org.webpieces.router.api.simplesvr.MtgRouteId.ARGS_MISMATCH;
import static org.webpieces.router.api.simplesvr.MtgRouteId.SOME_EXAMPLE;

import org.webpieces.router.api.routing.RouteModule;
import org.webpieces.router.api.routing.Router;

public class CommonRouteModule implements RouteModule {

	@Override
	public void configure(Router router, String packageName) {
		//We cannot do this or the compiler in dev router will compile it too early for testing
		//String controllerName = SomeController.class.getName();

		router.addRoute(GET, "/user/{id}",  "org.webpieces.devrouter.api.CommonController.badRedirect", SOME_EXAMPLE);
		router.addRoute(GET, "/something",  "org.webpieces.devrouter.api.CommonController.argsMismatch", ARGS_MISMATCH);
		router.addPostRoute("/postroute",    "org.webpieces.devrouter.api.CommonController.postReturnsHtmlRender");
		
		//router.addRoute(POST,     "/{controller}/{action}", "{controller}.post{action}", null);
		
		router.setNotFoundRoute("org.webpieces.devrouter.api.CommonController.notFound");
	}
}
