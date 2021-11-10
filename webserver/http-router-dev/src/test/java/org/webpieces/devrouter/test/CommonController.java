package org.webpieces.devrouter.test;

import org.webpieces.util.futures.XFuture;

import javax.inject.Singleton;
import javax.ws.rs.PathParam;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.Actions;
import org.webpieces.router.api.controller.actions.Redirect;
import org.webpieces.router.api.controller.actions.Render;
import org.webpieces.router.api.simplesvr.MtgRouteId;

@Singleton
public class CommonController {

	private boolean isWantRedirect = false;
	
	public Render notFound() {
		return Actions.renderThis();
	}
	public Render internalError() {
		throw new IllegalStateException("fail this for testing");
	}
	
	public Action argsMismatch(@PathParam("id") int id, @PathParam("accId") String accId) {
		return Actions.renderThis();
	}
	
	public Redirect badRedirect(@PathParam("id") int id) {
		
		//This is missing the id parameter
		return Actions.redirect(MtgRouteId.SOME_EXAMPLE);
	}
	
	public Action someExample(@PathParam("array") String ... args) {
		return Actions.renderThis();
	}
	
	public XFuture<Action> createUserForm() {
		//if for some reason, reached wrong thing or not enough users, redirect to another page....
		if(isWantRedirect) {
			return XFuture.completedFuture(Actions.redirect(SomeRouteId.GET_CREATE_USER_PAGE));
		}
		
		return XFuture.completedFuture(Actions.renderThis());
	}

//	public XFuture<Action> postUser(MeetingDto user) {
//		
//		//if user is !valid {
//		if(isWantRedirect) {
//			//flash.saveFormValues();
//			//flash.setGlobalMessage("You have errors")
//			//decorators kick in saying error per field with the field
//			return XFuture.completedFuture(new Redirect(SomeRouteId.GET_CREATE_USER_PAGE));
//		}
//		//}
//		
//		//need to send redirect at this point to getUser with id=id
//		return XFuture.completedFuture(new Redirect(SomeRouteId.GET_SHOW_USER));
//	}
	
	/**
	 * or if you have nothing asynchronous going on.....then we KISS ...
	 * 
	 * @param id
	 * @return
	 */
	public Action getUser(@PathParam("id") int id) {

		Object user = null; //in reality, this is a lookup from the database by id
		
		//here, we would redirect if the user is not found to some other page and add error to master error message
		
		//pass in User to the Render so it is given to the page...
		return Actions.renderThis(user);
	}
	
}
