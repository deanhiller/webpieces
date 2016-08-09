package org.webpieces.router.impl.actions;

import java.util.Arrays;
import java.util.List;

import org.webpieces.router.api.actions.Redirect;
import org.webpieces.router.api.dto.RedirectResponse;
import org.webpieces.router.api.routing.RouteId;

public class RedirectImpl implements Redirect {
	private RouteId id;
	private List<Object> args;
	private RedirectResponse redirectResponse;

	public RedirectImpl(RouteId id, Object ... args) {
		this.id = id;
		this.args = Arrays.asList(args);
	}

	public RedirectImpl(RedirectResponse redirectResponse) {
		this.redirectResponse = redirectResponse;
	}

	public RouteId getId() {
		return id;
	}

	public List<Object> getArgs() {
		return args;
	}

	public RedirectResponse getRedirectResponse() {
		return redirectResponse;
	}
}
