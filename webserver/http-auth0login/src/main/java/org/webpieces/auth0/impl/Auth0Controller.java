package org.webpieces.auth0.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.auth0.client.api.*;
import org.webpieces.router.api.controller.actions.Redirect;
import org.webpieces.util.futures.XFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Auth0Controller {

	private static final Logger log = LoggerFactory.getLogger(Auth0Controller.class);

	private Auth0Service auth0Service;

	@Inject
	public Auth0Controller(
			Auth0Service auth0Service
	) {
		this.auth0Service = auth0Service;
	}

	public Redirect logout() {
		return auth0Service.logout();

	}
	
	public Redirect login() throws Exception {
		return auth0Service.login();
	}

	public XFuture<Redirect> callback() throws Exception {
		return auth0Service.callback();
	}

}
