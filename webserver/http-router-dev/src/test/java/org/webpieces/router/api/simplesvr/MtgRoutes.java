package org.webpieces.router.api.simplesvr;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.ctx.api.HttpMethod.POST;
import static org.webpieces.router.api.routing.Port.BOTH;
import static org.webpieces.router.api.simplesvr.MtgRouteId.ASYNC_ROUTE;
import static org.webpieces.router.api.simplesvr.MtgRouteId.GET_SHOW_MTG;
import static org.webpieces.router.api.simplesvr.MtgRouteId.POST_MEETING;
import static org.webpieces.router.api.simplesvr.MtgRouteId.SOME_EXAMPLE;

import org.webpieces.router.api.routing.Routes;
import org.webpieces.router.impl.model.bldr.DomainRouteBuilder;
import org.webpieces.router.impl.model.bldr.RouteBuilder;

public class MtgRoutes implements Routes {
	
	@Override
	public void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder bldr = domainRouteBldr.getAllDomainsRouteBuilder();

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
		
		bldr.addRoute(BOTH, GET,      "/something",    "MeetingController.someExample",     SOME_EXAMPLE);
//		bldr.addRoute(BOTH, GET,      "/listuser",     "MeetingController.createUserForm",  GET_CREATE_USER_PAGE);
		bldr.addRoute(BOTH, POST,     "/meeting",      "MeetingController.postMeeting", POST_MEETING, false); //insecure
		bldr.addRoute(BOTH, GET,      "/meeting/{id}", "MeetingController.getMeeting",    GET_SHOW_MTG);
		bldr.addRoute(BOTH, GET,      "/async",        "MeetingController.asyncMethod",     ASYNC_ROUTE);
		
		//bldr.addRoute(POST,     "/{controller}/{action}", "{controller}.post{action}", null);
		
		bldr.setPageNotFoundRoute("MeetingController.notFound");
		bldr.setInternalErrorRoute("MeetingController.internalError");
	}

}
