package org.webpieces.webserver.filters.app;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.Actions;

import javax.inject.Singleton;

@Singleton
public class FiltersController {
	
	public Action home() {
		return Actions.renderThis();
	}
	
	public Action login() {
		return Actions.renderThis();
	}
	
}
