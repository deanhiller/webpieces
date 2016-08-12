package org.webpieces.router.api.error.dev;

import static org.webpieces.router.api.dto.HttpMethod.GET;
import static org.webpieces.router.api.simplesvr.MtgRouteId.ARGS_MISMATCH;
import static org.webpieces.router.api.simplesvr.MtgRouteId.SOME_EXAMPLE;

import org.webpieces.router.api.routing.AbstractRouteModule;

public class CommonRouteModule extends AbstractRouteModule {

	@Override
	public void configure(String packageName) {
		//We cannot do this or the compiler in dev router will compile it too early for testing
		//String controllerName = SomeController.class.getName();

		addRoute(GET, "/user/{id}",  "org.webpieces.devrouter.api.CommonController.badRedirect", SOME_EXAMPLE);
		addRoute(GET, "/something",  "org.webpieces.devrouter.api.CommonController.argsMismatch", ARGS_MISMATCH);
		
		//addRoute(POST,     "/{controller}/{action}", "{controller}.post{action}", null);
		
		setPageNotFoundRoute("org.webpieces.devrouter.api.CommonController.notFound");
		setInternalErrorRoute("org.webpieces.devrouter.api.CommonController.internalError");
	}
}
