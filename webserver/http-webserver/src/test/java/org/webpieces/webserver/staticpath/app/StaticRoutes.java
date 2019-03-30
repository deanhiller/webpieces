package org.webpieces.webserver.staticpath.app;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.router.api.routing.Port.BOTH;

import org.webpieces.router.api.routing.Routes;
import org.webpieces.router.impl.model.bldr.DomainRouteBuilder;
import org.webpieces.router.impl.model.bldr.RouteBuilder;

public class StaticRoutes implements Routes {

	@Override
	public void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder bldr = domainRouteBldr.getAllDomainsRouteBuilder();
		bldr.addRoute(BOTH, GET , "/pageparam",         "StaticController.home", StaticRouteId.PAGE_PARAM);
		
		bldr.addStaticFile(BOTH, "/public/myfile", "src/test/resources/tagsMeta.txt", false);
		bldr.addStaticFile(BOTH, "/public/mycss",  "src/test/resources/fortest.css", false);

		//relative path(to working directory)
		bldr.addStaticDir(BOTH, "/public/", "src/test/resources/staticRoutes/", false);
		
		bldr.setPageNotFoundRoute("/org/webpieces/webserver/basic/app/biz/BasicController.notFound");
		bldr.setInternalErrorRoute("/org/webpieces/webserver/basic/app/biz/BasicController.internalError");
	}

}
