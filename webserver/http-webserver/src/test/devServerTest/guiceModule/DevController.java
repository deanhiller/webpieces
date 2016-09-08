package org.webpieces.webserver.dev.app;

import javax.inject.Inject;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;

public class DevController {

	@Inject
	private NewInterface library;
	
	public Action home() {
		String user = library.fetchName();
		return Actions.renderThis("user", user);
	}

}
