package webpiecesxxxxxpackage.web.main;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.router.api.routes.Port.BOTH;
import static webpiecesxxxxxpackage.web.main.MainRouteId.ASYNC_ROUTE;
import static webpiecesxxxxxpackage.web.main.MainRouteId.MAIN_ROUTE;
import static webpiecesxxxxxpackage.web.main.MainRouteId.SYNC_ROUTE;

import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routes.Routes;

public class MainRoutes implements Routes {

	@Override
	public void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder bldr = domainRouteBldr.getAllDomainsRouteBuilder();
		//The path parameter(3rd param) is a semi-regular expression that we match on.  We convert {...} to a
		//   regex a capture group for you BUT leave the rest untouched so you can do whatever regex you like
		//   ORDER matters so the order of modules is important and the order of routes is important

		bldr.addStaticDir(BOTH, "/assets/", "public/", false);
		//Add a single file by itself(not really needed)
		bldr.addStaticFile(BOTH, "/favicon.ico", "public/favicon.ico", false);
		bldr.addStaticFile(BOTH, "/test.css", "public/crud/fonts.css", false);

		//Remove the next 3 lines if you are doing react and uncomment the react routes near the bottom.
		bldr.addRoute(BOTH, GET, "/",              "MainController.index", MAIN_ROUTE);
		bldr.addRoute(BOTH, GET, "/sync",         "MainController.mySyncMethod", SYNC_ROUTE);
		bldr.addRoute(BOTH, GET, "/async",         "MainController.myAsyncMethod", ASYNC_ROUTE); //for advanced users who want to release threads to do more work

		//Use these routes for react.  In Order, here is what they do
		//1. / just routes https://domain.com, https://domain.com/ to index.html
		//2. This one routes any path with a '.' in it like main.js into the react folder for a static file
		//3. Any other route is routed to react/index.html so the refresh button does not give a 404 and yields the
		//    page the user is on.
//		bldr.addStaticFile(BOTH, "/", "react/index.html", false);
//		bldr.addStaticDir(BOTH, "(?<resource>.*\\..*)", "react/", false);
//		bldr.addStaticFile(BOTH, "/.*", "react/index.html", false);

		bldr.setPageNotFoundRoute("MainController.notFound");
		bldr.setInternalErrorRoute("MainController.internalError");
	}

}
