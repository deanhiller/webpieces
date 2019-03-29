package org.webpieces.router.impl.model;

import java.util.Map;

public class MatchResult2 {

	private final Map<String, String> pathParams;
	
	public MatchResult2(Map<String, String> pathParams) {
		this.pathParams = pathParams;
	}

	public Map<String, String> getPathParams() {
		return pathParams;
	}
	
	@Override
	public String toString() {
		return "MatchResult [pathParams=" + pathParams +"]";
	}

}
