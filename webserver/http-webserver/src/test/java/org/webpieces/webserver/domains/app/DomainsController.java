package org.webpieces.webserver.domains.app;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.Actions;
import org.webpieces.router.api.controller.actions.Render;

import javax.inject.Singleton;

@Singleton
public class DomainsController {
	
	public Action domain1() {
		return Actions.renderThis();
	}
	
	public Action domain2() {
		return Actions.renderThis();
	}
	
	public Render notFoundDomain1() {
		return Actions.renderThis();
	}
}
