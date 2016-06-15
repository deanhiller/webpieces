package org.webpieces.devrouter.api;

import java.util.concurrent.CompletableFuture;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Redirect;
import org.webpieces.router.api.actions.Render;
import org.webpieces.router.api.routing.Param;
import org.webpieces.router.api.simplesvr.MtgRouteId;

public class SomeController {

	private boolean isWantRedirect = false;
	
	public void notFound() {
	}
	
	public Action argsMismatch(@Param("id") int id, @Param("accId") String accId) {
		return new Render();
	}
	
	public Redirect badRedirect(@Param("id") int id) {
		
		//This is missing the id parameter
		return new Redirect(MtgRouteId.SOME_EXAMPLE);
	}
	
	public Action someExample(@Param("array") String ... args) {
		return new Render();
	}
	
	public CompletableFuture<Action> createUserForm() {
		//if for some reason, reached wrong thing or not enough users, redirect to another page....
		if(isWantRedirect) {
			return CompletableFuture.completedFuture(new Redirect(SomeRouteId.GET_CREATE_USER_PAGE));
		}
		
		return CompletableFuture.completedFuture(new Render());
	}

//	public CompletableFuture<Action> postUser(MeetingDto user) {
//		
//		//if user is !valid {
//		if(isWantRedirect) {
//			//flash.saveFormValues();
//			//flash.setGlobalMessage("You have errors")
//			//decorators kick in saying error per field with the field
//			return CompletableFuture.completedFuture(new Redirect(SomeRouteId.GET_CREATE_USER_PAGE));
//		}
//		//}
//		
//		//need to send redirect at this point to getUser with id=id
//		return CompletableFuture.completedFuture(new Redirect(SomeRouteId.GET_SHOW_USER));
//	}
	
	/**
	 * or if you have nothing asynchronous going on.....then we KISS ...
	 * 
	 * @param id
	 * @return
	 */
	public Action getUser(@Param("id") int id) {

		Object user = null; //in reality, this is a lookup from the database by id
		
		//here, we would redirect if the user is not found to some other page and add error to master error message
		
		//pass in User to the Render so it is given to the page...
		return new Render(user);
	}
	
}
