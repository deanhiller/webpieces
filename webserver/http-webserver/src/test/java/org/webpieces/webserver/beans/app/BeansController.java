package org.webpieces.webserver.beans.app;

import javax.inject.Inject;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;
import org.webpieces.router.api.actions.Redirect;
import org.webpieces.router.api.ctx.Request;
import org.webpieces.router.api.ctx.RequestContext;
import org.webpieces.webserver.basic.biz.SomeLib;
import org.webpieces.webserver.basic.biz.SomeOtherLib;
import org.webpieces.webserver.basic.biz.UserDbo;

public class BeansController {
	
	@Inject
	private SomeLib lib1;
	@Inject
	private SomeOtherLib lib;
	
	public Action urlEncoding(String user) {
		return Actions.renderThis("user", user);
	}
	
	public Action pageParam() {
		return Actions.renderThis("user", "Dean Hiller");
	}

	public Redirect postUser(UserDbo user, String password) {
		//Validate any other stuff you need here adding errors and such
		lib1.validateUser(user);
		
		RequestContext ctx = Request.getContext();
		if(Request.validation().hasErrors()) {
			return Actions.redirectFlashAll(BeansRouteId.USER_FORM_ROUTE, ctx);
		}
		
		lib.saveUser(user);
		return Actions.redirect(BeansRouteId.LIST_USERS_ROUTE);
	}
	
	public Action listUsers() {
		return Actions.renderThis();
	}
	
	public Action userForm() {
		return Actions.renderThis();
	}
}
