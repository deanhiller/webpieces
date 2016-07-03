package PACKAGE;

import org.webpieces.router.api.dto.HttpMethod;
import org.webpieces.router.api.routing.RouteModule;
import org.webpieces.router.api.routing.Router;

public class CLASSNAMERouteModule implements RouteModule {

	//This is just ONE 
	@Override
	public void configure(Router router, String currentPackage) {

		//You can always do a platform override on ControllerResolver.java as well to resolve the controller String in some other
		//way
		
		//local controller(same package as your RouteModule!!!!)
		router.addRoute(HttpMethod.GET, "/", "CLASSNAMELocalController.someMethod", CLASSNAMERouteId.SOME_ROUTE);
		
		//relative path to Controller from your RouteModule....
		router.addRoute(HttpMethod.GET, "/redirect/{id}", "example/CLASSNAMEController.redirect", CLASSNAMERouteId.REDIRECT_PAGE);

		//absolute path from root of classpath...
		router.addRoute(HttpMethod.GET, "/myroute", "/PACKAGE/example/CLASSNAMEController.myMethod", CLASSNAMERouteId.RENDER_PAGE);

		//relative going down a level and back up relative to your RouteModule
		router.setNotFoundRoute("../PACKAGE/example/CLASSNAMEController.notFound");
	}

}
