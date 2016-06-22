package org.webpieces.webserver.api;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;

public class MyTestController {

	public Action redirect() {
		return Actions.redirect(MyTestRouteId.RENDER_PAGE);
	}
	
	public Action render() {
		return Actions.renderThis("testing");
	}
	
	public Action notFound() {
		return Actions.renderThis();
	}
}
