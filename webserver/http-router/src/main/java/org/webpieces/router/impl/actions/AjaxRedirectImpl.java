package org.webpieces.router.impl.actions;

import java.util.Map;

import org.webpieces.router.api.controller.actions.AjaxRedirect;
import org.webpieces.router.api.routes.RouteId;

public class AjaxRedirectImpl implements AjaxRedirect {
	private RouteId id;
	private Map<String, Object> args;

	public AjaxRedirectImpl(RouteId id, Object ... args) {
		this.id = id;
		this.args = PageArgListConverter.createPageArgMap(args);
	}

	public RouteId getId() {
		return id;
	}

	public Map<String, Object> getArgs() {
		return args;
	}

}
