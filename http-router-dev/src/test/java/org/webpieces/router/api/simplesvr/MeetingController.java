package org.webpieces.router.api.simplesvr;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;
import org.webpieces.router.api.actions.Redirect;
import org.webpieces.router.api.routing.Param;

public class MeetingController {

	private boolean isWantRedirect = false;
	
	@Inject
	private SomeUtil util;
	@Inject
	private SomeService service;
	
	public void notFound() {
	}
	
	public Action someExample() {
		util.testMethod();
		return Actions.redirect(MtgRouteId.SOME_EXAMPLE);
	}
	
	public CompletableFuture<Action> createUserForm() {
		//if for some reason, reached wrong thing or not enough users, redirect to another page....
		if(isWantRedirect) {
			return CompletableFuture.completedFuture(Actions.redirect(MtgRouteId.GET_CREATE_MTG_PAGE));
		}
		
		return CompletableFuture.completedFuture(Actions.renderThis());
	}

	public CompletableFuture<Action> postMeeting(/* @Param("mtg") MeetingDto mtg */) {
		
		//if user is !valid {
		if(isWantRedirect) {
			//flash.saveFormValues();
			//flash.setGlobalMessage("You have errors")
			//decorators kick in saying error per field with the field
			return CompletableFuture.completedFuture(Actions.redirect(MtgRouteId.GET_CREATE_MTG_PAGE));
		}
		//}
		
		//need to send redirect at this point to getUser with id=id
		return CompletableFuture.completedFuture(Actions.redirect(MtgRouteId.GET_SHOW_MTG, 888));
	}
	
	/**
	 * or if you have nothing asynchronous going on.....then we KISS ...
	 * 
	 * @param id
	 * @return
	 */
	public Action getMeeting(@Param("id") int id) {

		//MeetingDto user = null; //in reality, this is a lookup from the database by id
		
		//here, we would redirect if the user is not found to some other page and add error to master error message

		//here we could call getMeeting(id) so refactorings would apply BUT then we have to bytecode the crap
		//out of the code so we don't call getMeeting and actually throw an exception back to the platform like play
		//I wish there was a better way as I don't like either choice
		return Actions.redirect(MtgRouteId.GET_SHOW_MTG, 999);
	}
	
	public CompletableFuture<Redirect> asyncMethod() {
		return service.remoteCall()
			.thenApply(val -> Actions.redirect(MtgRouteId.GET_SHOW_MTG, val));
	}
	
}
