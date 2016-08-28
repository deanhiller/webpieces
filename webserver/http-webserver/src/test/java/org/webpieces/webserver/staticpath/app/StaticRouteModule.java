package org.webpieces.webserver.staticpath.app;

import static org.webpieces.ctx.api.HttpMethod.GET;

import org.webpieces.router.api.routing.AbstractRouteModule;

public class StaticRouteModule extends AbstractRouteModule {

	@Override
	public void configure(String currentPackage) {
		addRoute(GET , "/pageparam",         "StaticController.home", StaticRouteId.PAGE_PARAM);
		
		String property = System.getProperty("user.dir");
		
		//absolute path...
		addStaticFile("/public/myfile", property + "/src/test/resources/tagsMeta.txt", false);
		//relative path(to user.dir)
		addStaticDir("/public/", "src/test/resources/", false);
		
		setPageNotFoundRoute("../../basic/biz/BasicController.notFound");
		setInternalErrorRoute("../../basic/biz/BasicController.internalError");
	}

}
