package WEBPIECESxPACKAGE.base;

import static WEBPIECESxPACKAGE.base.examples.ExamplesRouteId.ASYNC_ROUTE;
import static WEBPIECESxPACKAGE.base.examples.ExamplesRouteId.LIST_EXAMPLES;
import static WEBPIECESxPACKAGE.base.examples.ExamplesRouteId.MAIN_ROUTE;
import static WEBPIECESxPACKAGE.base.examples.ExamplesRouteId.MAIN_ROUTE2;
import static WEBPIECESxPACKAGE.base.examples.ExamplesRouteId.MAIN_ROUTE3;
import static WEBPIECESxPACKAGE.base.examples.ExamplesRouteId.REDIRECT_PAGE;
import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.router.api.routing.Port.BOTH;

import org.webpieces.router.api.routing.Routes;
import org.webpieces.router.impl.model.bldr.DomainRouteBuilder;
import org.webpieces.router.impl.model.bldr.RouteBuilder;

import WEBPIECESxPACKAGE.base.examples.ExamplesRouteId;

public class AppRoutes implements Routes {

	@Override
	public void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder router = domainRouteBldr.getAllDomainsRouteBuilder();
		//You can always do a platform override on ControllerResolver.java as well to resolve the controller String in some other
		//way.  The controller string is the 3rd parameter in the addRoute calls
		
		//The path parameter(2nd param) is a semi-regular expression that we match on.  We convert {...} to a 
		//   regex a capture group for you BUT leave the rest untouched so you can whatever regex you like
		//   ORDER matters so the order of modules is important and the order of routes
		//
		//The controller.method is a relative or absolute path with ClassName.method at the end
		//RouteIds are used to redirect in the webapp itself and must be unique
		router.addRoute(BOTH, GET, "/",              "examples/ExamplesController.index", MAIN_ROUTE);
		router.addRoute(BOTH, GET, "/main2",         "/WEBPIECESxPACKAGE/base/examples/ExamplesController.index", MAIN_ROUTE2);
		router.addRoute(BOTH, GET, "/main3",         "WEBPIECESxPACKAGE.base.examples.ExamplesController.index", MAIN_ROUTE3);
		
		router.addRoute(BOTH, GET, "/home",          "crud/login/AppLoginController.index", ExamplesRouteId.HOME);
		router.addRoute(BOTH, GET, "/tags",          "crud/login/AppLoginController.tags", ExamplesRouteId.TAGS);

		router.addRoute(BOTH, GET, "/user/{name}", "examples/ExamplesController.loadUser", ExamplesRouteId.LOAD_USER);    

		router.addRoute(BOTH, GET, "/examples",      "examples/ExamplesController.exampleList", LIST_EXAMPLES);      //local controller(same package as your RouteModule!!!!)
		router.addRoute(BOTH, GET, "/redirect/{id}", "examples/ExamplesController.redirect", REDIRECT_PAGE);    //shows a redirect example in the controller method
		router.addRoute(BOTH, GET, "/async",         "examples/ExamplesController.myAsyncMethod", ASYNC_ROUTE); //for advanced users who want to release threads to do more work

		router.addStaticDir(BOTH, "/assets/", "public/", false);
		//Add a single file by itself(not really needed)
		router.addStaticFile(BOTH, "/favicon.ico", "public/favicon.ico", false);
		router.addStaticFile(BOTH, "/test.css", "public/crud/fonts.css", false);

		router.setPageNotFoundRoute("examples/ExamplesController.notFound");
		router.setInternalErrorRoute("examples/ExamplesController.internalError");
	}

}
