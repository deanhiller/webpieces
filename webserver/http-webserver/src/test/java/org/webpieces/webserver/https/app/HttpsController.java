package org.webpieces.webserver.https.app;

import javax.inject.Singleton;

import org.webpieces.ctx.api.Current;
import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;
import org.webpieces.router.api.actions.Redirect;

@Singleton
public class HttpsController {
	
	public Action home() {
		return Actions.renderThis();
	}
	
	public Action internal() {
		return Actions.renderThis();
	}
	
	public Action login() {
		return Actions.renderThis();
	}
	
	public Redirect postLogin() {
		//simulating successful login here...
		Current.session().put(HttpsRouteModule.LOGIN_TOKEN, "someId");
		
		String url = Current.flash().get("url");
		if(url != null) {
			return Actions.redirectToUrl(url); //page the user was trying to access before logging in
		}
		return Actions.redirect(HttpsRouteId.HOME); //base login page
	}
	
	public Action httpRoute() {
		return Actions.renderThis();
	}
	public Action httpsRoute() {
		return Actions.renderThis();
	}
	
	
}
