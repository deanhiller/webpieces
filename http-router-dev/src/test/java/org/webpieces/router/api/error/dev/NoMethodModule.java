package org.webpieces.router.api.error.dev;

import static org.webpieces.router.api.dto.HttpMethod.getAll;
import static org.webpieces.router.api.simplesvr.MtgRouteId.SOME_EXAMPLE;

import org.webpieces.compiler.simple.SomeController;
import org.webpieces.router.api.routing.RouteModule;
import org.webpieces.router.api.routing.Router;

public class NoMethodModule implements RouteModule {
	
	@Override
	public void configure(Router router, String packageName) {
		String controllerName = SomeController.class.getName();

		router.addRoute(getAll(), "/something",  controllerName+".thisMethodNotExist", SOME_EXAMPLE);
		
		//router.addRoute(POST,     "/{controller}/{action}", "{controller}.post{action}", null);
		
		router.setCatchAllRoute("MeetingController.notFound");
	}
}
