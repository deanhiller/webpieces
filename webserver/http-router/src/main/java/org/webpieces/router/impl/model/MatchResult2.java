package org.webpieces.router.impl.model;

import java.util.Map;

public class MatchResult2 {

	private final Map<String, String> pathParams;
	private final boolean matches;
	
	public MatchResult2(boolean matches) {
		this.matches = matches;
		pathParams = null;
	}
	
	public MatchResult2(Map<String, String> pathParams) {
		this.pathParams = pathParams;
		this.matches = true;
	}

	public Map<String, String> getPathParams() {
		return pathParams;
	}
	
	@Override
	public String toString() {
		return "MatchResult [pathParams=" + pathParams +"]";
	}

	public boolean isMatches() {
		return matches;
	}

}
