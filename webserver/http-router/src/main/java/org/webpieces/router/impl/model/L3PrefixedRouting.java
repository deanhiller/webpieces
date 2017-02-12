package org.webpieces.router.impl.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.webpieces.router.impl.RouteMeta;

public class L3PrefixedRouting {

	final Map<String, L3PrefixedRouting> pathPrefixToInfo = new HashMap<>();
	
	List<RouteMeta> routes = new ArrayList<>();

	public L3PrefixedRouting getScopedRouter(String path) {
		L3PrefixedRouting r = pathPrefixToInfo.get(path);
		if(r == null) {
			r = new L3PrefixedRouting();
			pathPrefixToInfo.put(path, r);
		}
		return r;
	}

	public void addRoute(RouteMeta meta) {
		
	}

	
	
}
