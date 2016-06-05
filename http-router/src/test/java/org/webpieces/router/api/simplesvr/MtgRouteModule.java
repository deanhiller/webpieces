package org.webpieces.router.api.simplesvr;

import static org.webpieces.router.api.dto.HttpMethod.GET;
import static org.webpieces.router.api.dto.HttpMethod.POST;
import static org.webpieces.router.api.dto.HttpMethod.getAll;
import static org.webpieces.router.api.simplesvr.MtgRouteId.GET_CREATE_USER_PAGE;
import static org.webpieces.router.api.simplesvr.MtgRouteId.GET_SHOW_USER;
import static org.webpieces.router.api.simplesvr.MtgRouteId.POST_CREATE_USER;
import static org.webpieces.router.api.simplesvr.MtgRouteId.SOME_EXAMPLE;

import org.webpieces.router.api.RouteModule;
import org.webpieces.router.api.Router;

public class MtgRouteModule implements RouteModule {
	
	@Override
	public void configure(Router router, String packageName) {

		router.addRoute(getAll(), "/something",  "MeetingController.someExample",     SOME_EXAMPLE);
		router.addRoute(GET,      "/createuser", "MeetingController.createUserForm",  GET_CREATE_USER_PAGE);
		router.addRoute(POST,     "/createuser", "MeetingController.postUser",        POST_CREATE_USER);
		router.addRoute(GET,      "/user/:id",   "MeetingController.getUser",         GET_SHOW_USER);
		
		router.addRoute(POST,     "/{controller}/{action}", "#{{controller}.post{action}}", null);
		
		router.setCatchAllRoute("MeetingController.notFound()");
	}
}
