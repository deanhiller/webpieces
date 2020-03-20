package org.webpieces.webserver.json.app;

import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routes.Routes;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.ctx.api.HttpMethod.POST;
import static org.webpieces.router.api.routes.Port.BOTH;

public class JsonRoutes implements Routes {

	@Override
	public void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder bldr = domainRouteBldr.getAllDomainsRouteBuilder();
		
		bldr.addContentRoute(BOTH, GET , "/json/read",         "JsonController.readOnly");
		bldr.addContentRoute(BOTH, POST, "/json/write",        "JsonController.writeOnly");
		bldr.addContentRoute(BOTH, POST, "/json/writeasync",   "JsonController.writeAsync");
		
		bldr.addContentRoute(BOTH, GET , "/json/{id}",         "JsonController.jsonRequest");
		bldr.addContentRoute(BOTH, POST , "/json/{id}",        "JsonController.postJson");

		bldr.addContentRoute(BOTH, GET , "/json/async/{id}",   "JsonController.asyncJsonRequest");
		bldr.addContentRoute(BOTH, POST, "/json/async/{id}",   "JsonController.postAsyncJson");

		bldr.addContentRoute(BOTH, GET , "/json/throw/{id}",        "JsonController.throwNotFound");

		bldr.setPageNotFoundRoute("/org/webpieces/webserver/basic/app/biz/BasicController.notFound");
		bldr.setInternalErrorRoute("/org/webpieces/webserver/basic/app/biz/BasicController.internalError");
	}

}
