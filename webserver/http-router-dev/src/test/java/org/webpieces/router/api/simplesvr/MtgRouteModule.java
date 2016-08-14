package org.webpieces.router.api.simplesvr;

import static org.webpieces.ctx.api.HttpMethod.*;
import static org.webpieces.router.api.simplesvr.MtgRouteId.*;

import org.webpieces.router.api.routing.RouteModule;
import org.webpieces.router.api.routing.Router;

public class MtgRouteModule implements RouteModule {
	
	@Override
	public void configure(Router router, String packageName) {

		//A typical CRUD list of routes is
		//   1. GET list users or     GET  /listusers
		//   2. GET add user page or  GET  /user
		//   3. GET edit user page or GET  /user/{id}
		//   4. POST save user or     POST /user
		//   5. GET display user or   GET  /displayuser (optional though as edit user shows the user in an editable mode)
		
		//ok, a few different scenarios
		//   1. /user/{var1}/{var2}/{var3}             Controller.method() and controller accesses RequestLocal.getRequest().getParams().get("var1");
		//   2. /user/{var1}/{var2}/{var3}             Controller.method(var1, var2, var3)
		//   3. /user/{var1}?var2=xx&var3=yyy&cat=dog  Controller.method(var1) and controller accesses RequestLocal.getRequest().getParams().get("var2");
        //   4. /user?var1=xxx&var1=yyy                Controller.method({xxx, yyy}) as an array
		//
		//ON TOP of this, do you maintain a separate structure for params IN THE PATH /user/{var1} vs in the query params /user/{var1}?var1=xxx
		//
		//AND ON TOP of that, we have multi-part fields as well with keys and values for POSTs
		
		router.addRoute(getAll(), "/something",    "MeetingController.someExample",     SOME_EXAMPLE);
//		router.addRoute(GET,      "/listuser",     "MeetingController.createUserForm",  GET_CREATE_USER_PAGE);
		router.addRoute(POST,     "/meeting",      "MeetingController.postMeeting", POST_MEETING);
		router.addRoute(GET,      "/meeting/{id}", "MeetingController.getMeeting",    GET_SHOW_MTG);
		router.addRoute(GET,      "/async",        "MeetingController.asyncMethod",     ASYNC_ROUTE);
		
		//router.addRoute(POST,     "/{controller}/{action}", "{controller}.post{action}", null);
		
		router.setPageNotFoundRoute("MeetingController.notFound");
		router.setInternalErrorRoute("MeetingController.internalError");
	}
}
