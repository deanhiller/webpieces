package org.webpieces.webserver.staticpath.app;

import static org.webpieces.ctx.api.HttpMethod.GET;

import org.webpieces.router.api.routing.AbstractRoutes;

public class StaticRoutes extends AbstractRoutes {

	@Override
	public void configure() {
		addRoute(GET , "/pageparam",         "StaticController.home", StaticRouteId.PAGE_PARAM);
		
		addStaticFile("/public/myfile", "src/test/resources/tagsMeta.txt", false);
		addStaticFile("/public/mycss",  "src/test/resources/fortest.css", false);

		//relative path(to working directory)
		addStaticDir("/public/", "src/test/resources/staticRoutes/", false);
		
		setPageNotFoundRoute("/org/webpieces/webserver/basic/app/biz/BasicController.notFound");
		setInternalErrorRoute("/org/webpieces/webserver/basic/app/biz/BasicController.internalError");
	}

}
