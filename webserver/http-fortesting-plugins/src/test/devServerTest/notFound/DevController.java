package org.webpieces.webserver.dev.app;

import javax.inject.Singleton;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;

@Singleton
public class DevController {

	public Action home() {
		return Actions.renderThis("user", "Dean Hiller");
	}

	public Action existingRoute() {
		return Actions.renderThis();
	}
	
	public Action notFound() {
		return Actions.renderThis("value", "something2");
	}
	
	public Action causeError() {
		throw new RuntimeException("testing");
	}
	
	public Action internalError() {
		return Actions.renderThis("error", "error1");
	}
}
