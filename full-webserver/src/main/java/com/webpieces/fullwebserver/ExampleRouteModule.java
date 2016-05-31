package com.webpieces.fullwebserver;

import static com.webpieces.fullwebserver.ExampleRouteId.GET_CREATE_USER_PAGE;
import static com.webpieces.fullwebserver.ExampleRouteId.GET_SHOW_USER;
import static com.webpieces.fullwebserver.ExampleRouteId.POST_CREATE_USER;
import static com.webpieces.fullwebserver.ExampleRouteId.SOME_EXAMPLE;
import static org.webpieces.router.api.dto.HttpMethod.GET;
import static org.webpieces.router.api.dto.HttpMethod.POST;
import static org.webpieces.router.api.dto.HttpMethod.getAll;

import javax.inject.Inject;

import org.webpieces.router.api.RouteModule;
import org.webpieces.router.api.Router;

public class ExampleRouteModule implements RouteModule {

	@Inject
	private HomeController controllerA;
	
	@Override
	public void configure(Router router) {

		Router scoped = router.getScopedRouter("/store", true);

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
		//8. Have a global POST route that works for every controller.  It was nice never defining the post routes in play
		//POST       /{controller}/{action}                  {controller}.post{action}
		//# Routes for all ajaxAddEdit and delete stuff
		//GET     /{controller}/ajaxAddEdit/{id}          {controller}.ajaxAddEdit
		//GET     /{controller}/ajaxDelete/{id}           {controller}.ajaxDelete

		//# Catch all
		//POST       /{controller}/{action}                  {controller}.post{action}
		


		//CRUD...
		//1. /user  - creating a user?   
		//2. /user/:id - GET...display non-editable
		//3. /users - separate route
		//4. /user/:id - ....GET
		//5. POST /edituser
		
		router.addRoute(getAll(), "/something",  "#{HomeControllerA.someExample}", SOME_EXAMPLE);
		scoped.addRoute(GET,      "/createuser", "#{controllerA.createUserForm}",  GET_CREATE_USER_PAGE);
		scoped.addRoute(POST,     "/createuser", "#{controllerA.postUser}",        POST_CREATE_USER);
		scoped.addRoute(GET,      "/user/:id",   "#{controllerA.getUser}",         GET_SHOW_USER);
		
		scoped.addRoute(POST,     "/{controller}/{action}", "#{{controller}.post{action}}", null);
		
		router.addFilter("/secure/*", new SecurityFilter());
		
	}
}
