package WEBPIECESxPACKAGE.base;

import static WEBPIECESxPACKAGE.base.crud.CrudUserRouteId.GET_ADD_USER_FORM;
import static WEBPIECESxPACKAGE.base.crud.CrudUserRouteId.GET_EDIT_USER_FORM;
import static WEBPIECESxPACKAGE.base.crud.CrudUserRouteId.LIST_USERS;
import static WEBPIECESxPACKAGE.base.crud.CrudUserRouteId.POST_DELETE_USER;
import static WEBPIECESxPACKAGE.base.crud.CrudUserRouteId.POST_USER_FORM;
import static WEBPIECESxPACKAGE.base.crud.CrudUserRouteId.CONFIRM_DELETE_USER;

import static WEBPIECESxPACKAGE.base.examples.ExamplesRouteId.ASYNC_ROUTE;
import static WEBPIECESxPACKAGE.base.examples.ExamplesRouteId.LIST_EXAMPLES;
import static WEBPIECESxPACKAGE.base.examples.ExamplesRouteId.MAIN_ROUTE;
import static WEBPIECESxPACKAGE.base.examples.ExamplesRouteId.MAIN_ROUTE2;
import static WEBPIECESxPACKAGE.base.examples.ExamplesRouteId.MAIN_ROUTE3;
import static WEBPIECESxPACKAGE.base.examples.ExamplesRouteId.REDIRECT_PAGE;
import static org.webpieces.ctx.api.HttpMethod.GET;

import org.webpieces.router.api.routing.AbstractRouteModule;

public class AppRouteModule extends AbstractRouteModule {

	@Override
	public void configure() {
		//You can always do a platform override on ControllerResolver.java as well to resolve the controller String in some other
		//way.  The controller string is the 3rd parameter in the addRoute calls
		
		//The path parameter(2nd param) is a semi-regular expression that we match on.  We convert {...} to a 
		//   regex a capture group for you BUT leave the rest untouched so you can whatever regex you like
		//   ORDER matters so the order of modules is important and the order of routes
		//
		//The controller.method is a relative or absolute path with ClassName.method at the end
		//RouteIds are used to redirect in the webapp itself and must be unique
		addRoute(GET, "/",              "examples/ExamplesController.index", MAIN_ROUTE);
		addRoute(GET, "/main2",         "/WEBPIECESxPACKAGE/base/examples/ExamplesController.index", MAIN_ROUTE2);
		addRoute(GET, "/main3",         "WEBPIECESxPACKAGE.base.examples.ExamplesController.index", MAIN_ROUTE3);

		addRoute(GET, "/examples",      "examples/ExamplesController.exampleList", LIST_EXAMPLES);      //local controller(same package as your RouteModule!!!!)
		addRoute(GET, "/redirect/{id}", "examples/ExamplesController.redirect", REDIRECT_PAGE);    //shows a redirect example in the controller method
		addRoute(GET, "/async",         "examples/ExamplesController.myAsyncMethod", ASYNC_ROUTE); //for advanced users who want to release threads to do more work

		//Add where all the html files exist
		String workingDir = System.getProperty("user.dir");
		addStaticDir("/public/", workingDir+"/public/", false);
		//Add a single file by itself(not really needed)
		addStaticFile("/favicon.ico", "public/favicon.ico", false);

		setPageNotFoundRoute("examples/ExamplesController.notFound");
		setInternalErrorRoute("examples/ExamplesController.internalError");
	}

}
