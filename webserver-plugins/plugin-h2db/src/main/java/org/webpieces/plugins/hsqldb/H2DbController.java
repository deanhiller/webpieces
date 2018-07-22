package org.webpieces.plugins.hsqldb;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.router.api.actions.Actions;
import org.webpieces.router.api.actions.Render;

@Singleton
public class H2DbController {

	private ServerConfig config;

	@Inject
	public H2DbController(ServerConfig config) {
		this.config = config;
	}
	
	public Render databaseGui() {
		return Actions.renderThis("port", config.getPort());
	}
}
