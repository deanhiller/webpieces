package org.webpieces.router.api.actions;

import java.util.Arrays;
import java.util.List;

import org.webpieces.router.api.routing.RouteId;

public class Redirect implements Action {

	private RouteId id;
	private List<Object> args;

	protected Redirect(RouteId id, Object ... args) {
		this.id = id;
		this.args = Arrays.asList(args);
	}

	public RouteId getId() {
		return id;
	}

	public List<Object> getArgs() {
		return args;
	}
}
