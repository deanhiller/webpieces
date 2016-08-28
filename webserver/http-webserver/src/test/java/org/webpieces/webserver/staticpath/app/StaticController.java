package org.webpieces.webserver.staticpath.app;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;

public class StaticController {
	
	public Action home() {
		return Actions.renderThis();
	}
}
