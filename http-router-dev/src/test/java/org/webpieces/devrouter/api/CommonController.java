package org.webpieces.devrouter.api;

import java.util.concurrent.CompletableFuture;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;
import org.webpieces.router.api.actions.Redirect;
import org.webpieces.router.api.routing.Param;
import org.webpieces.router.api.simplesvr.MtgRouteId;

public class CommonController {

	private boolean isWantRedirect = false;
	
	public Action notFound() {
		return Actions.renderView("notFound.xhtml");
	}
	
	public Action postReturnsHtmlRender() {
		return Actions.renderThis();
	}
	
	public Action argsMismatch(@Param("id") int id, @Param("accId") String accId) {
		return Actions.renderThis();
	}
	
	public Redirect badRedirect(@Param("id") int id) {
		
		//This is missing the id parameter
		return Actions.redirect(MtgRouteId.SOME_EXAMPLE);
	}
	
	public Action someExample(@Param("array") String ... args) {
		return Actions.renderThis();
	}
	
	public CompletableFuture<Action> createUserForm() {
		//if for some reason, reached wrong thing or not enough users, redirect to another page....
		if(isWantRedirect) {
			return CompletableFuture.completedFuture(Actions.redirect(SomeRouteId.GET_CREATE_USER_PAGE));
		}
		
		return CompletableFuture.completedFuture(Actions.renderThis());
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
		return Actions.renderThis(user);
	}
	
}
