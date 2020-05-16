package org.webpieces.router.impl.actions;

import org.webpieces.router.api.controller.actions.AjaxRedirect;
import org.webpieces.router.api.routes.RouteId;

public class AjaxRedirectImpl implements AjaxRedirect {
	private RouteId id;
	private Object[] args;

	public AjaxRedirectImpl(RouteId id, Object ... args) {
		this.id = id;
		this.args = args;
	}

	public RouteId getId() {
		return id;
	}

	public Object[] getArgs() {
		return args;
	}

}
