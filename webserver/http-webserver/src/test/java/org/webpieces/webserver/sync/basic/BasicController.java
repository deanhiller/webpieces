package org.webpieces.webserver.sync.basic;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.webserver.sync.BasicRouteId;

public class BasicController {

	public Action someMethod() {
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
		return Actions.renderThis();
	}
	
	public Action internalError() {
		return Actions.renderThis();
	}
	
}
