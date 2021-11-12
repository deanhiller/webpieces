package org.webpieces.router.api.simplesvr;

import org.webpieces.util.futures.XFuture;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.PathParam;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.Actions;
import org.webpieces.router.api.controller.actions.Redirect;
import org.webpieces.router.api.controller.actions.Render;

@Singleton
public class MeetingController {

	private boolean isWantRedirect = false;
	
	private final SomeUtil util;
	private final SomeService service;
	
	@Inject
	public MeetingController(SomeUtil util, SomeService service) {
		super();
		this.util = util;
		this.service = service;
	}
	
	public Render notFound() {
		return Actions.renderThis();
	}
	
	public Render internalError() {
		return Actions.renderThis();
	}
	
	public Action someExample() {
		util.testMethod();
		return Actions.redirect(MtgRouteId.SOME_EXAMPLE);
	}
	
	public XFuture<Action> createUserForm() {
		//if for some reason, reached wrong thing or not enough users, redirect to another page....
		if(isWantRedirect) {
			return XFuture.completedFuture(Actions.redirect(MtgRouteId.GET_CREATE_MTG_PAGE));
		}
		
		return XFuture.completedFuture(Actions.renderThis());
	}

	public XFuture<Redirect> postMeeting(/* @Param("mtg") MeetingDto mtg */) {
		
		//if user is !valid {
		if(isWantRedirect) {
			//flash.saveFormValues();
			//flash.setGlobalMessage("You have errors")
			//decorators kick in saying error per field with the field
			return XFuture.completedFuture(Actions.redirect(MtgRouteId.GET_CREATE_MTG_PAGE));
		}
		//}
		
		//need to send redirect at this point to getUser with id=id
		return XFuture.completedFuture(Actions.redirect(MtgRouteId.GET_SHOW_MTG, "id", 888));
	}
	
	/**
	 * or if you have nothing asynchronous going on.....then we KISS ...
	 * 
	 * @param id
	 * @return
	 */
	public Action getMeeting(@PathParam("id") int id) {

		//MeetingDto user = null; //in reality, this is a lookup from the database by id
		
		//here, we would redirect if the user is not found to some other page and add error to master error message

		//here we could call getMeeting(id) so refactorings would apply BUT then we have to bytecode the crap
		//out of the code so we don't call getMeeting and actually throw an exception back to the platform like play
		//I wish there was a better way as I don't like either choice
		return Actions.redirect(MtgRouteId.GET_SHOW_MTG, 999);
	}
	
	public XFuture<Redirect> asyncMethod() {
		return service.remoteCall()
			.thenApply(val -> Actions.redirect(MtgRouteId.GET_SHOW_MTG, "id", val));
	}
	
}
