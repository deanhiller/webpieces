package org.webpieces.plugins.fortesting;

import javax.inject.Singleton;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.Actions;

@Singleton
public class BasicController {

	public Action notFound() {
		return Actions.renderThis();
	}
	
	public Action internalError() {
		return Actions.renderThis();
	}
	
}
