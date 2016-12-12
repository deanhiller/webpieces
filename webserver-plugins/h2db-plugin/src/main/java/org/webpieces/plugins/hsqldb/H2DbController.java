package org.webpieces.plugins.hsqldb;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.router.api.actions.Actions;
import org.webpieces.router.api.actions.Redirect;

@Singleton
public class H2DbController {

	private H2DbConfig config;

	@Inject
	public H2DbController(H2DbConfig config) {
		this.config = config;
	}
	
	public Redirect renderDatabaseGui() {
		return Actions.redirectToUrl("http://localhost:"+config.getPort());
	}
}
