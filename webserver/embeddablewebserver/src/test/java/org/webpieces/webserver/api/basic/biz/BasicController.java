package org.webpieces.webserver.api.basic.biz;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;
import org.webpieces.webserver.api.basic.BasicRouteId;

public class BasicController {

	public Action someMethod() {
		return Actions.redirect(BasicRouteId.RENDER_PAGE);
	}
	
	public Action redirect(String id) {
		return Actions.redirect(BasicRouteId.RENDER_PAGE);
	}
	
	public Action myMethod() {
		//renderThis assumes the view is the <methodName>.html file so in this case
		//myMethod.html which must be in the same directory as the Controller
		return Actions.renderThis("hhhh");
	}
	
	public Action notFound() {
		return Actions.renderThis();
	}
	
}
