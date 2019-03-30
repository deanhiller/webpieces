package org.webpieces.router.api.error.dev;

import javax.inject.Singleton;

import org.webpieces.router.api.controller.actions.Actions;
import org.webpieces.router.api.controller.actions.Render;

@Singleton
public class ErrorController {
	
	public Render notFound() {
		return Actions.renderThis();
	}
	
	public Render internalError() {
		return Actions.renderThis();
	}
	
}
