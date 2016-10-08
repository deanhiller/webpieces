package org.webpieces.router.impl.actions;

import java.util.Map;

import org.webpieces.router.api.actions.Redirect;
import org.webpieces.router.api.dto.RedirectResponse;
import org.webpieces.router.api.routing.RouteId;

public class RedirectImpl implements Redirect {
	private RouteId id;
	private Map<String, Object> args;
	private RedirectResponse redirectResponse;

	public RedirectImpl(RouteId id, Object ... args) {
		this.id = id;
		this.args = PageArgListConverter.createPageArgMap(args);
	}

	public RedirectImpl(RedirectResponse redirectResponse) {
		this.redirectResponse = redirectResponse;
	}

	public RouteId getId() {
		return id;
	}

	public Map<String, Object> getArgs() {
		return args;
	}

	public RedirectResponse getRedirectResponse() {
		return redirectResponse;
	}
}
