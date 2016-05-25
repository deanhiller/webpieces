package com.webpieces.fullwebserver;

import static com.webpieces.fullwebserver.ExampleRouteId.GET_CREATE_USER_PAGE;
import static com.webpieces.fullwebserver.ExampleRouteId.GET_SHOW_USER;
import static com.webpieces.fullwebserver.ExampleRouteId.POST_CREATE_USER;
import static com.webpieces.fullwebserver.ExampleRouteId.SOME_EXAMPLE;
import static org.webpieces.router.api.HttpMethod.GET;
import static org.webpieces.router.api.HttpMethod.POST;
import static org.webpieces.router.api.HttpMethod.getAllHttpMethods;

import javax.inject.Inject;

import org.webpieces.router.api.RouteModule;
import org.webpieces.router.api.Router;

public class ExampleRouteModule implements RouteModule {

	@Inject
	private HomeController controllerA;
	
	@Override
	public void configure(Router router) {
		
		//Issues to resolve...
		//1. how to define page is only accessible over https (seam does pattern path="/secure/*" scheme="https" while we
		//     have more flexibility here as the developer uses an api we provide(we can allow registration of hooks in the router)
		//2. page is only allowed if logged in with role (could be multiple roles...need to pass in a lamba closure maybe?)
		//3. adding interceptor like seam?
		//4. if we know the class UserDbo.class needed to pass in AND the variable name used in the method, we should be
		//     able to translae the form into the UserDbo for the user which is very useful
		//5. some form of validator (JSR303) will need to be plugged in as well for #4 such that the user can define error messages
		//6. eventually need to incorporate eclipse compiler at some point(this could be tricky based on 
		//   current direction?..need to investigate)
		//7. I can't find a better way as of yet than providing controller AND methodString for the controller due to the
		//things that need to happen.  
		
		router.addRoute(SOME_EXAMPLE,         getAllHttpMethods(), "/something", controllerA, "someExample");
		router.addRoute(GET_CREATE_USER_PAGE, GET,                 "/createuser", controllerA, "createUserForm");
		router.addRoute(POST_CREATE_USER,     POST, "/createuser", controllerA, "postUser");
		router.addRoute(GET_SHOW_USER,        GET, "/user/:id", controllerA, "getUser");
		
		router.addFilter("/secure/*", new SecurityFilter());
		
	}
}
