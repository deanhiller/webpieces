package org.webpieces.webserver.api;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;

public class MyTestController {

	public Action redirect() {
		return Actions.redirect(MyTestRouteId.RENDER_PAGE);
	}
	
	public Action render() {
		return Actions.renderThis("6666");
	}
	
	public Action notFound() {
		return Actions.renderThis();
	}
}
