package PACKAGE.example;

import org.webpieces.router.api.dto.HttpMethod;
import org.webpieces.router.api.routing.RouteModule;
import org.webpieces.router.api.routing.Router;

import PACKAGE.CLASSNAMERouteId;

public class CLASSNAMERouteModule implements RouteModule {

	@Override
	public void configure(Router router, String currentPackage) {

		//You can always do a platform override on ControllerResolver.java as well to resolve the controller String in some other
		//way.  The controller string is the 3rd parameter in most of these calls
		
		//That path is a semi-regular expression that we match on
		//The controller.method is a relative or absolute path with ClassName.method at the end
		//RouteIds are used to redirect in the webapp itself and must be unique
		router.addRoute(HttpMethod.GET, "/",              "CLASSNAMELocalController.someMethod", CLASSNAMERouteId.SOME_ROUTE);      //local controller(same package as your RouteModule!!!!)
		router.addRoute(HttpMethod.GET, "/nextexample",   "extra/ExtraController.relativeController", CLASSNAMERouteId.EXTRA_ROUTE); //shows a controller relative to THIS module
		router.addRoute(HttpMethod.GET, "/another",       "../CLASSNAMEController.anotherMethod", CLASSNAMERouteId.ANOTHER);  //relative path from THIS module to Controller going down
		router.addRoute(HttpMethod.GET, "/absolute",      "/PACKAGE/CLASSNAMEController.myMethod", CLASSNAMERouteId.ABSOLUTE);    //absolute path from root of classpath...
		router.addRoute(HttpMethod.GET, "/absolute2",     "PACKAGE.CLASSNAMEController.myMethod", CLASSNAMERouteId.ABSOLUTE2);    //we don't advise this as it is not consistent with relative paths but it can be done if you like
		router.addRoute(HttpMethod.GET, "/redirect/{id}", "CLASSNAMELocalController.redirect", CLASSNAMERouteId.REDIRECT_PAGE);    //shows a redirect example in the controller method
		router.addRoute(HttpMethod.GET, "/async",         "CLASSNAMELocalController.myAsyncMethod", CLASSNAMERouteId.ASYNC_ROUTE); //for advanced users who want to release threads to do more work

		router.setPageNotFoundRoute("CLASSNAMELocalController.notFound");
		router.setInternalErrorRoute("CLASSNAMELocalController.internalError");
	}

}
