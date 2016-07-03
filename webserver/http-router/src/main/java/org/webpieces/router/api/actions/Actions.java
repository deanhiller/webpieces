package org.webpieces.router.api.actions;

import org.webpieces.router.api.routing.RouteId;

public class Actions {

	public static RenderHtml renderView(String view, Object ... pageArgs) {
		return new RenderHtml(view, pageArgs);
	}

	public static RenderHtml renderThis(Object ... pageArgs) {
		return new RenderHtml(pageArgs);
	}

	public static Redirect redirect(RouteId routeId, Object ... args) {
		return new Redirect(routeId, args);
	}
	
}
