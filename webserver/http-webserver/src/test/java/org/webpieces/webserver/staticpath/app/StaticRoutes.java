package org.webpieces.webserver.staticpath.app;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.router.api.routing.Port.BOTH;

import org.webpieces.router.api.routing.Routes;
import org.webpieces.router.impl.model.bldr.DomainRouteBuilder;
import org.webpieces.router.impl.model.bldr.RouteBuilder;

public class StaticRoutes implements Routes {

	@Override
	public void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder router = domainRouteBldr.getAllDomainsRouteBuilder();
		router.addRoute(BOTH, GET , "/pageparam",         "StaticController.home", StaticRouteId.PAGE_PARAM);
		
		router.addStaticFile(BOTH, "/public/myfile", "src/test/resources/tagsMeta.txt", false);
		router.addStaticFile(BOTH, "/public/mycss",  "src/test/resources/fortest.css", false);

		//relative path(to working directory)
		router.addStaticDir(BOTH, "/public/", "src/test/resources/staticRoutes/", false);
		
		router.setPageNotFoundRoute("/org/webpieces/webserver/basic/app/biz/BasicController.notFound");
		router.setInternalErrorRoute("/org/webpieces/webserver/basic/app/biz/BasicController.internalError");
	}

}
