package org.webpieces.webserver.domains.app;

import static org.webpieces.ctx.api.HttpMethod.GET;

import org.webpieces.router.api.routing.AbstractRouteModule;

public class Domain2Module extends AbstractRouteModule {

	@Override
	public void configure() {
		addRoute(GET ,     "/domain2",             "DomainsController.domain2", DomainsRouteId.DOMAIN2);
		
		setPageNotFoundRoute("/org/webpieces/webserver/basic/app/biz/BasicController.notFound");
		setInternalErrorRoute("/org/webpieces/webserver/basic/app/biz/BasicController.internalError");
	}

}
