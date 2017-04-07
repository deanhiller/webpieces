package org.webpieces.webserver.staticpath.app;

import static org.webpieces.ctx.api.HttpMethod.GET;

import org.webpieces.router.api.routing.AbstractRoutes;

public class StaticRoutes extends AbstractRoutes {

	@Override
	public void configure() {
		addRoute(GET , "/pageparam",         "StaticController.home", StaticRouteId.PAGE_PARAM);
		
		String property = System.getProperty("user.dir");
		
		//absolute path...
		addStaticFile("/public/myfile", property + "/src/test/resources/tagsMeta.txt", false);
		//relative path(to user.dir)
		addStaticDir("/public/", "src/test/resources/staticRoutes/", false);
		
		setPageNotFoundRoute("/org/webpieces/webserver/basic/app/biz/BasicController.notFound");
		setInternalErrorRoute("/org/webpieces/webserver/basic/app/biz/BasicController.internalError");
	}

}
