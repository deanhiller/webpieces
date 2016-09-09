package org.webpieces.webserver.dev.app;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;

public class DevController {

	public Action home() {
		return Actions.renderThis("user", "Dean Hiller");
	}

	public Action existingRoute() {
		return Actions.renderThis();
	}
}
