package org.webpieces.webserver.tokens;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.Actions;

import javax.inject.Singleton;

@Singleton
public class TokenController {
	
	public Action requiredNotExist() {
		return Actions.renderThis();
	}
	
	public Action optionalNotExist() {
		return Actions.renderThis();
	}

	public Action optionalNotExist2() {
		return Actions.renderThis();
	}
	
	public Action optionalAndNull() {
		return Actions.renderThis("client", null);
	}
	
	public Action requiredAndNull() {
		return Actions.renderThis("client", null);
	}
	
	public Action escapingTokens() {
		//The & html will be escaped so it shows up to the user as & (ie. in html it is &amp; unless verbatim is used .. 
		return Actions.renderThis("someHtml", "<h1>Some& Title</h1>");
	}
}
