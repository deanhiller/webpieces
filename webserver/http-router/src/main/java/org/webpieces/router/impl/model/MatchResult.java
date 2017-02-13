package org.webpieces.router.impl.model;

import java.util.HashMap;
import java.util.Map;

import org.webpieces.router.impl.RouteMeta;

public class MatchResult {

	private final Map<String, String> pathParams;
	private final RouteMeta meta;
	private boolean isNotFound;
	
	public MatchResult(RouteMeta meta, Map<String, String> pathParams) {
		this.meta = meta;
		this.pathParams = pathParams;
	}

	public MatchResult(RouteMeta meta) {
		this.meta = meta;
		this.pathParams = new HashMap<>();
	}

	public MatchResult(boolean isNotFound) {
		this(null, null);
		this.isNotFound = isNotFound;
	}

	public RouteMeta getMeta() {
		return meta;
	}

	public Map<String, String> getPathParams() {
		return pathParams;
	}
	
	@Override
	public String toString() {
		return "MatchResult [pathParams=" + pathParams + ", meta=" + meta + " notFound="+isNotFound+"]";
	}

	public boolean isNotFound() {
		return isNotFound;
	}

}
