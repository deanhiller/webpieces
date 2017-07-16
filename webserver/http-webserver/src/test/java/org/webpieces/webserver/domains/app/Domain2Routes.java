package org.webpieces.webserver.domains.app;

import static org.webpieces.ctx.api.HttpMethod.GET;

import org.webpieces.router.api.routing.AbstractRoutes;

public class Domain2Routes extends AbstractRoutes {

	@Override
	public void configure() {
		addRoute(GET ,     "/domain2",             "DomainsController.domain2", DomainsRouteId.DOMAIN2);
		
		addStaticFile("/public/myfile", "src/test/resources/tagsMeta.txt", false);
		//relative path(relative to baseDirectory in RouterConfig)
		addStaticDir("/public/", "src/test/resources/staticRoutes/", false);
		
		setPageNotFoundRoute("/org/webpieces/webserver/basic/app/biz/BasicController.notFound");
		setInternalErrorRoute("/org/webpieces/webserver/basic/app/biz/BasicController.internalError");
	}

}
