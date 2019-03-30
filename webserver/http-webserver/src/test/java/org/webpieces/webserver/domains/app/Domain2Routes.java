package org.webpieces.webserver.domains.app;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.router.api.routing.Port.BOTH;

import org.webpieces.router.api.routing.Routes;
import org.webpieces.router.impl.model.bldr.DomainRouteBuilder;
import org.webpieces.router.impl.model.bldr.RouteBuilder;

public class Domain2Routes implements Routes {

	@Override
	public void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder bldr = domainRouteBldr.getAllDomainsRouteBuilder();
		bldr.addRoute(BOTH, GET ,     "/domain2",             "DomainsController.domain2", DomainsRouteId.DOMAIN2);
		
		bldr.addStaticFile(BOTH, "/public2/myfile", "src/test/resources/tagsMeta.txt", false);
		//relative path(relative to baseDirectory in RouterConfig)
		bldr.addStaticDir(BOTH, "/public2/", "src/test/resources/staticRoutes/", false);
		
		bldr.setPageNotFoundRoute("/org/webpieces/webserver/basic/app/biz/BasicController.notFound");
		bldr.setInternalErrorRoute("/org/webpieces/webserver/basic/app/biz/BasicController.internalError");
	}

}
