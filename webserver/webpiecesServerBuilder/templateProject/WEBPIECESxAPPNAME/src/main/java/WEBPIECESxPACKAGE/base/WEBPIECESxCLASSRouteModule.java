package WEBPIECESxPACKAGE.base;

import static org.webpieces.ctx.api.HttpMethod.*;

import org.webpieces.router.api.routing.AbstractRouteModule;

import WEBPIECESxPACKAGE.base.routes.ExampleRouteId;
import WEBPIECESxPACKAGE.base.routes.WEBPIECESxCLASSRouteId;

public class WEBPIECESxCLASSRouteModule extends AbstractRouteModule {

	@Override
	public void configure(String currentPackage) {

		//You can always do a platform override on ControllerResolver.java as well to resolve the controller String in some other
		//way.  The controller string is the 3rd parameter in the addRoute calls
		
		//That path is a semi-regular expression that we match on
		//The controller.method is a relative or absolute path with ClassName.method at the end
		//RouteIds are used to redirect in the webapp itself and must be unique
		addRoute(GET, "/",              "example/WEBPIECESxCLASSLocalController.index", WEBPIECESxCLASSRouteId.MAIN_ROUTE);
		
		addRoute(GET, "/examples",      "example/WEBPIECESxCLASSLocalController.exampleList", WEBPIECESxCLASSRouteId.SOME_ROUTE);      //local controller(same package as your RouteModule!!!!)
		addRoute(GET, "/nextexample",   "extra/ExtraController.relativeController", WEBPIECESxCLASSRouteId.EXTRA_ROUTE); //shows a controller relative to THIS module
		addRoute(GET, "/another",       "extra/WEBPIECESxCLASSController.anotherMethod", WEBPIECESxCLASSRouteId.ANOTHER);  //relative path from THIS module to Controller going down
		addRoute(GET, "/absolute",      "/WEBPIECESxPACKAGE/base/extra/WEBPIECESxCLASSController.myMethod", WEBPIECESxCLASSRouteId.ABSOLUTE);    //absolute path from root of classpath...
		addRoute(GET, "/absolute2",     "WEBPIECESxPACKAGE.base.extra.WEBPIECESxCLASSController.myMethod", WEBPIECESxCLASSRouteId.ABSOLUTE2);    //we don't advise this as it is not consistent with relative paths but it can be done if you like
		addRoute(GET, "/redirect/{id}", "example/WEBPIECESxCLASSLocalController.redirect", WEBPIECESxCLASSRouteId.REDIRECT_PAGE);    //shows a redirect example in the controller method
		addRoute(GET, "/async",         "example/WEBPIECESxCLASSLocalController.myAsyncMethod", WEBPIECESxCLASSRouteId.ASYNC_ROUTE); //for advanced users who want to release threads to do more work
		
		//basic crud example(or create your own crud method to create consistent crud operations)
		addCrud("user", "crud/ExampleController", 
				ExampleRouteId.LIST_USERS, ExampleRouteId.ADD_USER_FORM, ExampleRouteId.EDIT_USER_FORM, 
				ExampleRouteId.POST_USER_FORM, ExampleRouteId.POST_DELETE_USER);
		
		addStaticFile("/favicon.ico", "public/favicon.ico", false);
		String workingDir = System.getProperty("user.dir");
		addStaticDir("/public/", workingDir+"/public/", false);
		
		setPageNotFoundRoute("example/WEBPIECESxCLASSLocalController.notFound");
		setInternalErrorRoute("example/WEBPIECESxCLASSLocalController.internalError");
	}

}
