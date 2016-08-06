package org.webpieces.webserver.basic.biz;

import javax.inject.Inject;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.webserver.basic.BasicRouteId;

public class BasicController {

	@Inject
	private NotFoundLib notFoundLib;
	@Inject
	private InternalSvrErrorLib errorLib;
	
	public Action someMethod() {
		notFoundLib.someBusinessLogic();
		return Actions.redirect(BasicRouteId.RENDER_PAGE);
	}
	
	public Action redirect(String id) {
		return Actions.redirect(BasicRouteId.RENDER_PAGE);
	}
	
	public Action redirectWithInt(int id) {
		return Actions.redirect(BasicRouteId.RENDER_PAGE);
	}

	public Action throwNotFound() {
		throw new NotFoundException("not found");
	}
	
	public Action myMethod() {
		//renderThis assumes the view is the <methodName>.html file so in this case
		//myMethod.html which must be in the same directory as the Controller
		return Actions.renderThis("hhhh", 86);
	}
	
	public Action notFound() {
		//we use this to mock and throw NotFoundException or some RuntimeException for testing notFound path failures
		notFoundLib.someBusinessLogic();
		return Actions.renderThis();
	}
	
	public Action internalError() {
		//we use this to mock and throw exceptions when needed for testing
		errorLib.someBusinessLogic();
		return Actions.renderThis();
	}
	
	public Action pageParam() {
		return Actions.renderThis("user", "Dean Hiller");
	}

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
}
