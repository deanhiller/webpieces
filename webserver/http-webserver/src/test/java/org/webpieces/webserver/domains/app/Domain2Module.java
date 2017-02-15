package org.webpieces.webserver.domains.app;

import static org.webpieces.ctx.api.HttpMethod.GET;

import org.webpieces.router.api.routing.AbstractRouteModule;

public class Domain2Module extends AbstractRouteModule {

	@Override
	public void configure() {
		addRoute(GET ,     "/domain2",             "DomainsController.domain2", DomainsRouteId.DOMAIN2);
		
		
		String property = System.getProperty("user.dir");
		
		//absolute path...
		addStaticFile("/public/myfile", property + "/src/test/resources/tagsMeta.txt", false);
		//relative path(to user.dir)
		addStaticDir("/public/", "src/test/resources/staticRoutes/", false);
		
		setPageNotFoundRoute("/org/webpieces/webserver/basic/app/biz/BasicController.notFound");
		setInternalErrorRoute("/org/webpieces/webserver/basic/app/biz/BasicController.internalError");
	}

}
