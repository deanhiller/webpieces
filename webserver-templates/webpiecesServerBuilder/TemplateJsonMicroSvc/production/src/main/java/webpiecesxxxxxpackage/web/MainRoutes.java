package webpiecesxxxxxpackage.web;

import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routes.Routes;

import static org.webpieces.router.api.routes.Port.BOTH;

public class MainRoutes implements Routes {

	@Override
	public void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder bldr = domainRouteBldr.getAllDomainsRouteBuilder();
		//The path parameter(3rd param) is a semi-regular expression that we match on.  We convert {...} to a
		//   regex a capture group for you BUT leave the rest untouched so you can do whatever regex you like
		//   ORDER matters so the order of modules is important and the order of routes is important

		bldr.addStaticDir(BOTH, "/assets/", "public/", false);

		bldr.setPageNotFoundRoute("MainController.notFound");
		bldr.setInternalErrorRoute("MainController.internalError");
	}

}
