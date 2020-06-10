package org.webpieces.webserver.dev.app;

import javax.inject.Singleton;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.Actions;
import org.webpieces.router.api.controller.actions.Render;

@Singleton
public class DevController {

	public Action home() {
		return Actions.renderThis("user", "Dean Hiller");
	}

	public Action existingRoute() {
		return Actions.renderThis();
	}
	
	public Render notFound() {
		return Actions.renderThis("value", "something2");
	}
	
	public Action causeError() {
		throw new RuntimeException("testing");
	}
	
	public Render internalError() {
		return Actions.renderThis("error", "error1");
	}
	
	public Action filter() {
		return Actions.renderThis();
	}
	
	public void addedMethodForFun() {
	}
}
