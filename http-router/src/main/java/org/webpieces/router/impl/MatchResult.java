package org.webpieces.router.impl;

import java.util.HashMap;
import java.util.Map;

public class MatchResult {

	private final Map<String, String> pathParams;
	private final RouteMeta meta;
	
	public MatchResult(RouteMeta meta, Map<String, String> pathParams) {
		this.meta = meta;
		this.pathParams = pathParams;
	}

	public MatchResult(RouteMeta meta) {
		this.meta = meta;
		this.pathParams = new HashMap<>();
	}

	public RouteMeta getMeta() {
		return meta;
	}

	public Map<String, String> getPathParams() {
		return pathParams;
	}

}
