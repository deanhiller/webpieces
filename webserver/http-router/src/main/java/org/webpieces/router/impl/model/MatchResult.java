package org.webpieces.router.impl.model;

import java.util.HashMap;
import java.util.Map;

import org.webpieces.router.impl.RouteMeta;

public class MatchResult {

	private final Map<String, String> pathParams;
	private final RouteMeta meta;
	
	public MatchResult(RouteMeta meta, Map<String, String> pathParams) {
		if(meta == null)
			throw new IllegalArgumentException("must have meta.  null meta means pageNotFound so use other constructor");
		this.meta = meta;
		this.pathParams = pathParams;
	}

	public MatchResult(RouteMeta meta) {
		this(meta, new HashMap<>());
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

	public boolean isFound() {
		return meta != null;
	}

}
