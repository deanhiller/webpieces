package org.webpieces.googleauth.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.Session;
import org.webpieces.googleauth.api.GoogleAuthConfig;
import org.webpieces.googleauth.api.GoogleAuthPlugin;
import org.webpieces.router.api.controller.actions.Actions;
import org.webpieces.router.api.controller.actions.Redirect;
import org.webpieces.router.api.routes.RouteId;
import org.webpieces.util.futures.XFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AuthController {

	private static final Logger log = LoggerFactory.getLogger(AuthController.class);

	private AuthService auth0Service;
	protected final GoogleAuthConfig authRouteIdSet;

	@Inject
	public AuthController(
			AuthService auth0Service,
			GoogleAuthConfig authRouteIdSet
	) {
		this.auth0Service = auth0Service;
		this.authRouteIdSet = authRouteIdSet;
	}

	public Redirect logout() {
		return auth0Service.logout();

	}
	
	public Redirect login() throws Exception {
		Session session = Current.session();

		if(session.containsKey(GoogleAuthPlugin.USER_ID_TOKEN)) {
			//user is already logged in, so land him on the login homepage
			RouteId toRenderAfterLogin = authRouteIdSet.getToRenderAfterLogin();
			return Actions.redirect(toRenderAfterLogin); //base page after login screen
		}

		return auth0Service.login();
	}

	public XFuture<Redirect> callback() throws Exception {
		return auth0Service.callback();
	}

}
