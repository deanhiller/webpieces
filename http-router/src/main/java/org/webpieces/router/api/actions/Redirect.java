package org.webpieces.router.api.actions;

import java.util.HashMap;
import java.util.Map;

import org.webpieces.router.api.routing.RouteId;

public class Redirect implements Action {

	private RouteId id;
	private Map<String, String> keysToValues;

	public Redirect(RouteId id, Map<String, String> keysToValues) {
		this.id = id;
		this.keysToValues = keysToValues;
	}

	public Redirect(RouteId routeId) {
		this.id = routeId;
		this.keysToValues = new HashMap<>();
	}

	public RouteId getId() {
		return id;
	}

	public Map<String, String> getKeysToValues() {
		return keysToValues;
	}

}
