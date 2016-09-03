package PACKAGE.example;

import static org.webpieces.ctx.api.HttpMethod.*;

import org.webpieces.router.api.routing.AbstractRouteModule;

import PACKAGE.CLASSNAMERouteId;
import PACKAGE.ExampleRouteId;

public class CLASSNAMERouteModule extends AbstractRouteModule {

	@Override
	public void configure(String currentPackage) {

		//You can always do a platform override on ControllerResolver.java as well to resolve the controller String in some other
		//way.  The controller string is the 3rd parameter in the addRoute calls
		
		//That path is a semi-regular expression that we match on
		//The controller.method is a relative or absolute path with ClassName.method at the end
		//RouteIds are used to redirect in the webapp itself and must be unique
		addRoute(GET, "/",              "CLASSNAMELocalController.index", CLASSNAMERouteId.MAIN_ROUTE);
		
		addRoute(GET, "/examples",      "CLASSNAMELocalController.exampleList", CLASSNAMERouteId.SOME_ROUTE);      //local controller(same package as your RouteModule!!!!)
		addRoute(GET, "/nextexample",   "extra/ExtraController.relativeController", CLASSNAMERouteId.EXTRA_ROUTE); //shows a controller relative to THIS module
		addRoute(GET, "/another",       "../CLASSNAMEController.anotherMethod", CLASSNAMERouteId.ANOTHER);  //relative path from THIS module to Controller going down
		addRoute(GET, "/absolute",      "/PACKAGE/CLASSNAMEController.myMethod", CLASSNAMERouteId.ABSOLUTE);    //absolute path from root of classpath...
		addRoute(GET, "/absolute2",     "PACKAGE.CLASSNAMEController.myMethod", CLASSNAMERouteId.ABSOLUTE2);    //we don't advise this as it is not consistent with relative paths but it can be done if you like
		addRoute(GET, "/redirect/{id}", "CLASSNAMELocalController.redirect", CLASSNAMERouteId.REDIRECT_PAGE);    //shows a redirect example in the controller method
		addRoute(GET, "/async",         "CLASSNAMELocalController.myAsyncMethod", CLASSNAMERouteId.ASYNC_ROUTE); //for advanced users who want to release threads to do more work
		
		//basic crud example
		addRoute(GET,  "/userform",     "crud/ExampleController.userForm", ExampleRouteId.GET_USER_FORM);
		addRoute(POST, "/postuser",     "crud/ExampleController.postUser", ExampleRouteId.POST_USER_FORM);
		addRoute(GET,  "/listusers",    "crud/ExampleController.listUsers", ExampleRouteId.GET_LIST_USERS);
		
		String workingDir = System.getProperty("user.dir");
		addStaticDir("/public/", workingDir+"/public/", false);
		
		setPageNotFoundRoute("CLASSNAMELocalController.notFound");
		setInternalErrorRoute("CLASSNAMELocalController.internalError");
	}

}
