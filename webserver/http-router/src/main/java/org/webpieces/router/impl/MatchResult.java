package org.webpieces.router.impl;

import java.util.HashMap;
import java.util.Map;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.dto.MethodMeta;
import org.webpieces.util.filters.Service;

public class MatchResult {

	private final Map<String, String> pathParams;
	private final RouteMeta meta;
	
	public MatchResult(RouteMeta meta, Map<String, String> pathParams) {
		this.meta = meta;
		this.pathParams = pathParams;
	}

	public MatchResult(RouteMeta meta, Service<MethodMeta, Action> service) {
		this.meta = meta;
		this.pathParams = new HashMap<>();
	}

	public RouteMeta getMeta() {
		return meta;
	}

	public Map<String, String> getPathParams() {
		return pathParams;
	}
	
	@Override
	public String toString() {
		return "MatchResult [pathParams=" + pathParams + ", meta=" + meta + "]";
	}

}
