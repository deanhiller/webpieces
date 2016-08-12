package org.webpieces.webserver.beans.app;

import javax.inject.Inject;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;
import org.webpieces.router.api.actions.Redirect;
import org.webpieces.webserver.basic.biz.SomeOtherLib;
import org.webpieces.webserver.basic.biz.UserDbo;

public class BeansController {
	
	@Inject
	private SomeOtherLib lib;
	
	public Action urlEncoding(String user) {
		return Actions.renderThis("user", user);
	}
	
	public Action pageParam() {
		return Actions.renderThis("user", "Dean Hiller");
	}

	public Redirect postUser(UserDbo user) {
		lib.saveUser(user);
		return Actions.redirect(BeansRouteId.LIST_USERS_ROUTE);
	}
	
	public Action listUsers() {
		return Actions.renderThis();
	}
}
