package org.webpieces.webserver.api.login;

import org.webpieces.ctx.api.Current;
import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;
import org.webpieces.router.api.actions.Redirect;

public abstract class LoginController {
	
	public Action logout() {
		Current.session().remove(getLoginSessionKey());
		return Actions.redirect(LoginRouteId.LOGIN);
	}
	
	public Action login() {
		String user = Current.session().get(getLoginSessionKey());
		if(user != null)
			return Actions.redirect(LoginRouteId.LOGGED_IN_HOME);
		
		return fetchGetLoginPageAction(); //Actions.renderThis();
	}

	public Redirect postLogin(String username, String password) {
		
		boolean authenticated = isValidLogin(username, password);
		if(!authenticated || Current.validation().hasErrors()) {
			return Actions.redirectFlashAllSecure(LoginRouteId.LOGIN, Current.getContext(), "password");
		}
		
		//officially makes them logged in by putting the token in the session
		Current.session().put(getLoginSessionKey(), username);

		String url = Current.flash().get("url");
		if(url != null) {
			return Actions.redirectToUrl(url); //page the user was trying to access before logging in
		}
		return Actions.redirect(LoginRouteId.LOGGED_IN_HOME); //base page after login screen
	}

	protected String getLoginSessionKey() {
		return LoginInfo.LOGIN_TOKEN1;
	}
	
	protected abstract boolean isValidLogin(String username, String password);
	protected abstract Action fetchGetLoginPageAction();
	
}
