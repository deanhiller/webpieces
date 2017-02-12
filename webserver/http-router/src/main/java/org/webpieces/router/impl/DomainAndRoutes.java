package org.webpieces.router.impl;

import java.util.HashMap;
import java.util.Map;

import org.webpieces.router.impl.model.L3PrefixedRouting;

public class DomainAndRoutes {

	public DomainAndRoutes(String domainRegEx) {
		
	}
	
	private final Map<String, L3PrefixedRouting> pathPrefixToInfo = new HashMap<>();
	private L3PrefixedRouting routes = new L3PrefixedRouting();
	
}
