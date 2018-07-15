package org.webpieces.webserver.api.login;

import org.webpieces.ctx.api.Current;
import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;
import org.webpieces.router.api.actions.Redirect;
import org.webpieces.router.api.routing.RouteId;

public abstract class AbstractLoginController {
	
	public Action logout() {
		Current.session().remove(getLoginSessionKey());
		return Actions.redirect(getRenderLoginRoute());
	}
	
	public Action login() {
		String user = Current.session().get(getLoginSessionKey());
		if(user != null)
			return Actions.redirect(getRenderAfterLoginHome());
		
		Current.flash().keep(); //we must keep previous data like the url
		
		return fetchGetLoginPageAction(); //Actions.renderThis();
	}

	public Redirect postLogin(String username, String password) {
		
		boolean authenticated = isValidLogin(username, password);
		if(!authenticated || Current.validation().hasErrors()) {
			return Actions.redirectFlashAllSecure(getRenderLoginRoute(), Current.getContext(), "password");
		}
		
		//officially makes them logged in by putting the token in the session
		Current.session().put(getLoginSessionKey(), username);

		String url = Current.flash().get("url");
		if(url != null) {
			return Actions.redirectToUrl(url); //page the user was trying to access before logging in
		}
		return Actions.redirect(getRenderAfterLoginHome()); //base page after login screen
	}

	protected abstract String getLoginSessionKey();
	
	protected abstract boolean isValidLogin(String username, String password);
	protected abstract Action fetchGetLoginPageAction();

	protected abstract RouteId getRenderLoginRoute();
	protected abstract RouteId getRenderAfterLoginHome();
	
}
