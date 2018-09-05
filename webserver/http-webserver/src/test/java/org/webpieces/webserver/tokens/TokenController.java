package org.webpieces.webserver.tokens;

import javax.inject.Singleton;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;

@Singleton
public class TokenController {
	
	public Action requiredNotExist() {
		return Actions.renderThis();
	}
	
	public Action optionalNotExist() {
		return Actions.renderThis();
	}
	
	public Action optionalAndNull() {
		return Actions.renderThis("client", null);
	}
	
	public Action requiredAndNull() {
		return Actions.renderThis("client", null);
	}
}
