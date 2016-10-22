package org.webpieces.webserver.staticpath.app;

import javax.inject.Singleton;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;

@Singleton
public class StaticController {
	
	public Action home() {
		return Actions.renderThis();
	}
}
