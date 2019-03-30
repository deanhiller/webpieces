package org.webpieces.webserver.api.login;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.Actions;
import org.webpieces.router.api.controller.actions.Redirect;
import org.webpieces.router.api.routes.RouteId;

public abstract class AbstractLoginController {
	
	private String[] secureFields;
	
	public AbstractLoginController(String ... secureFields) {
		this.secureFields = secureFields;
	}

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
			RequestContext ctx = Current.getContext();
			Set<String> mySet = new HashSet<>(Arrays.asList(secureFields));
			ctx.moveFormParamsToFlash(mySet);
			ctx.getFlash().keep();
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
