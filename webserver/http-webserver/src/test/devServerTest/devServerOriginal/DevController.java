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
	
	public Action notFound() {
		return Actions.renderThis("value", "something1");
	}
	
	public Action causeError() {
		throw new RuntimeException("testing");
	}
	
	public Action internalError() {
		return Actions.renderThis("error", "error1");
	}
	
	public Action filter() {
		return Actions.renderThis();
	}
}
