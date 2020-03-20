package org.webpieces.webserver.staticpath.app;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.Actions;

import javax.inject.Singleton;

@Singleton
public class StaticController {
	
	public Action home() {
		return Actions.renderThis();
	}
}
