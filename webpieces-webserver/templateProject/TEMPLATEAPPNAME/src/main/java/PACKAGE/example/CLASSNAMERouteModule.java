package PACKAGE.example;

import org.webpieces.router.api.dto.HttpMethod;
import org.webpieces.router.api.routing.RouteModule;
import org.webpieces.router.api.routing.Router;

import PACKAGE.CLASSNAMERouteId;

public class CLASSNAMERouteModule implements RouteModule {

	//This is just ONE 
	@Override
	public void configure(Router router, String currentPackage) {
		//TEMP
		//relative path to Controller....
		router.addRoute(HttpMethod.GET, "/redirect/{id}", "CLASSNAMEController.redirect", CLASSNAMERouteId.REDIRECT_PAGE);

		//absolute path from root of classpath...
		router.addRoute(HttpMethod.GET, "/myroute", "CLASSNAMEController.myMethod", CLASSNAMERouteId.RENDER_PAGE);

		router.setNotFoundRoute("CLASSNAMEController.notFound");
		

		//If you don't like this, then implement a platform override for ControllerFinder.java and you will be passed
		//the controller string such that you can 
		
		//local controller(same package as RouteModule)
		//router.addRoute(HttpMethod.GET, "/somepathhere", "CLASSNAMELocalController.someMethod", CLASSNAMERouteId.SOME_ROUTE);
		
		//relative path to Controller....
		//router.addRoute(HttpMethod.GET, "/redirect/{id}", "example/CLASSNAMEController.redirect", CLASSNAMERouteId.REDIRECT_PAGE);

		//absolute path from root of classpath...
		//router.addRoute(HttpMethod.GET, "/myroute", "/PACKAGE/example/CLASSNAMEController.myMethod", CLASSNAMERouteId.RENDER_PAGE);

		//router.setNotFoundRoute("example/CLASSNAMEController.notFound");
	}

}
