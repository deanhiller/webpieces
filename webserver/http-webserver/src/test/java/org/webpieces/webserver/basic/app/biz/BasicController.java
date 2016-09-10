package org.webpieces.webserver.basic.app.biz;

import javax.inject.Inject;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.webserver.basic.app.BasicRouteId;

public class BasicController {

	@Inject
	private SomeOtherLib notFoundLib;
	@Inject
	private SomeLib errorLib;
	
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
	public Action badTemplate() {
		return Actions.renderThis();
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
	
	public Action jsonFile() {
		return Actions.renderView("basic.json");
	}
}
