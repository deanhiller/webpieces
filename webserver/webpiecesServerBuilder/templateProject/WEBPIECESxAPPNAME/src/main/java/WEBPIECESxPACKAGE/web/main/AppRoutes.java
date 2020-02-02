package WEBPIECESxPACKAGE.web.main;

import static WEBPIECESxPACKAGE.web.main.AppRouteId.ASYNC_ROUTE;
import static WEBPIECESxPACKAGE.web.main.AppRouteId.MAIN_ROUTE;
import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.router.api.routes.Port.BOTH;

import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routes.Routes;

public class AppRoutes implements Routes {

	@Override
	public void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder bldr = domainRouteBldr.getAllDomainsRouteBuilder();
		//The path parameter(3rd param) is a semi-regular expression that we match on.  We convert {...} to a
		//   regex a capture group for you BUT leave the rest untouched so you can do whatever regex you like
		//   ORDER matters so the order of modules is important and the order of routes is important

		//The Controller.method is a relative or absolute path with ClassName.method at the end
		//RouteIds are used to redirect in the webapp itself and must be unique
		bldr.addRoute(BOTH, GET, "/",              "MainController.index", MAIN_ROUTE);
		bldr.addRoute(BOTH, GET, "/async",         "MainController.myAsyncMethod", ASYNC_ROUTE); //for advanced users who want to release threads to do more work

		bldr.addStaticDir(BOTH, "/assets/", "public/", false);
		//Add a single file by itself(not really needed)
		bldr.addStaticFile(BOTH, "/favicon.ico", "public/favicon.ico", false);
		bldr.addStaticFile(BOTH, "/test.css", "public/crud/fonts.css", false);

		bldr.setPageNotFoundRoute("MainController.notFound");
		bldr.setInternalErrorRoute("MainController.internalError");
	}

}
