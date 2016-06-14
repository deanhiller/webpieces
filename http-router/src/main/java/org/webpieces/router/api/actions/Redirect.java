package org.webpieces.router.api.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.webpieces.router.api.routing.RouteId;

public class Redirect implements Action {

	private RouteId id;
	private List<Object> args;

	public Redirect(RouteId id, Object ... args) {
		this.id = id;
		this.args = Arrays.asList(args);
	}

	public Redirect(RouteId routeId) {
		this.id = routeId;
		this.args = new ArrayList<>();
	}

	public RouteId getId() {
		return id;
	}

	public List<Object> getArgs() {
		return args;
	}
}
