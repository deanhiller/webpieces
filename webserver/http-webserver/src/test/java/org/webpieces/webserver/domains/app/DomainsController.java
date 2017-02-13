package org.webpieces.webserver.domains.app;

import javax.inject.Singleton;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;
import org.webpieces.router.api.actions.Render;

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
