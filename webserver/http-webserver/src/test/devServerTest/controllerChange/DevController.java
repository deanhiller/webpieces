package org.webpieces.webserver.dev.app;

import javax.inject.Singleton;

import javax.inject.Singleton;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.Actions;
import org.webpieces.router.api.controller.actions.Render;

@Singleton
public class DevController {

	public Action home() {
		String user = "CoolJeff";
		return Actions.renderThis("user", user);
	}

	public Action existingRoute() {
		return Actions.renderThis();
	}
	
	public Render notFound() {
		return Actions.renderThis("value", "something1");
	}
	
	public Action causeError() {
		throw new RuntimeException("testing");
	}
	
	public Render internalError() {
		return Actions.renderThis("error", "error1");
	}
}
