package org.webpieces.webserver.api;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Redirect;
import org.webpieces.router.api.actions.RenderHtml;

public class MyTestController {

	public Action redirect() {
		return new Redirect(MyTestRouteId.RENDER_PAGE);
	}
	
	public Action render() {
		return RenderHtml.create("testing");
	}
	
	public Action notFound() {
		return RenderHtml.create();
	}
}
