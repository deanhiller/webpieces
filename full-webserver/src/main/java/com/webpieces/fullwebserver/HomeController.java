package com.webpieces.fullwebserver;

import java.util.concurrent.CompletableFuture;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Redirect;
import org.webpieces.router.api.actions.RenderHtml;

public class HomeController {

	private boolean isWantRedirect = false;
	
	public Action someExample(String ... args) {
		return RenderHtml.create();
	}
	
	public CompletableFuture<Action> createUserForm() {
		//if for some reason, reached wrong thing or not enough users, redirect to another page....
		if(isWantRedirect) {
			return CompletableFuture.completedFuture(new Redirect(ExampleRouteId.GET_CREATE_USER_PAGE));
		}
		
		return CompletableFuture.completedFuture(RenderHtml.create());
	}

	public CompletableFuture<Action> postUser(UserDto user) {
		
		//if user is !valid {
		if(isWantRedirect) {
			//flash.saveFormValues();
			//flash.setGlobalMessage("You have errors")
			//decorators kick in saying error per field with the field
			return CompletableFuture.completedFuture(new Redirect(ExampleRouteId.GET_CREATE_USER_PAGE));
		}
		//}
		
		//need to send redirect at this point to getUser with id=id
		return CompletableFuture.completedFuture(new Redirect(ExampleRouteId.GET_SHOW_USER));
	}
	
	/**
	 * or if you have nothing asynchronous going on.....then we KISS ...
	 * 
	 * @param id
	 * @return
	 */
	public Action getUser(int id) {

		UserDto user = null; //in reality, this is a lookup from the database by id
		
		//here, we would redirect if the user is not found to some other page and add error to master error message
		
		//pass in User to the Render so it is given to the page...
		return RenderHtml.create(user);
	}
	
}
