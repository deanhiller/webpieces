package org.webpieces.webserver.tags.app;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;
import org.webpieces.router.api.actions.Redirect;

public class TagController {
	public Action verbatimTag() {
		//The & html will be escaped so it shows up to the user as & (ie. in html it is &amp; unless verbatim is used .. 
		return Actions.renderThis("escaped", "'''escaped by default &'''", "verbatim", "'''verbatim & so do not escape'''");
	}
	
	public Action ifTag() {
		return Actions.renderThis("positive", "ThisExists", "negative", false, "negative2", null);
	}
	
	public Action elseTag() {
		return Actions.renderThis("positive", "ThisExists", "negative", false, "negative2", null);
	}

	public Action elseIfTag() {
		return Actions.renderThis("positive", "ThisExists", "negative", false, "negative2", null);
	}
	
	public Action getTag() {
		return Actions.renderThis("user", "Dean Hiller");
	}
	
	public Action extendsTag() {
		return Actions.renderThis("user", "Dean Hiller");
	}
	
	public Action aHrefTag() {
		return Actions.renderThis("user", "Dean Hiller");
	}
	
	public Action formTag() {
		return Actions.renderThis("user", "Dean");
	}
	
	public Redirect postSomething() {
		return Actions.redirect(TagsRouteId.FIELD_TAG);
	}
}
