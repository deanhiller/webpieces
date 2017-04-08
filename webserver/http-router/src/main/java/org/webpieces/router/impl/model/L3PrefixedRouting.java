package org.webpieces.router.impl.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.impl.RouteMeta;

public class L3PrefixedRouting {

	final Map<String, L3PrefixedRouting> pathPrefixToInfo = new HashMap<>();
	
	List<RouteMeta> routes = new ArrayList<>();

	public L3PrefixedRouting getScopedRouter(String fullPath) {
		String[] split = splitInTwo(fullPath);
		String path = fullPath;
		if(split != null)
			path = split[0];
		
		L3PrefixedRouting r = pathPrefixToInfo.get(path);
		if(r == null) {
			r = new L3PrefixedRouting();
			pathPrefixToInfo.put(path, r);
		}
		
		if(split == null)
			return r;
		else
			return r.getScopedRouter(split[1]);
	}

	private String[] splitInTwo(String fullPath) {
		if(!fullPath.startsWith("/"))
			throw new IllegalArgumentException("fullPath should start with a / but did not");
		
		int indexOf = fullPath.indexOf("/", 1);
		if(indexOf < 0)
			return null;
		
		String path = fullPath.substring(0, indexOf);
		String leftover = fullPath.substring(indexOf);
		return new String[] {path, leftover};
	}

	public void addRoute(RouteMeta meta) {
		routes.add(meta);
	}


	public MatchResult fetchRoute(RouterRequest request, String subPath) {
		if("".equals(subPath))
			return findRouteMatch(routes, request, subPath);
		else if(!subPath.startsWith("/"))
			throw new IllegalArgumentException("path must start with /");

		String prefix = subPath;
		int index = subPath.indexOf("/", 1);
		if(index == 1) {
			throw new IllegalArgumentException("path cannot start with //");
		} else if(index > 1) {
			prefix = subPath.substring(0, index);
		}

		L3PrefixedRouting routeInfo = pathPrefixToInfo.get(prefix);
		if(routeInfo != null) {
			if(index < 0)
				return routeInfo.fetchRoute(request, "");
			
			String newRelativePath = subPath.substring(index, subPath.length());
			MatchResult route = routeInfo.fetchRoute(request, newRelativePath);
			if(route != null)
				return route;
		}

		return findRouteMatch(routes, request, subPath);
	}

	public MatchResult findRouteMatch(List<RouteMeta> routes, RouterRequest request, String subPath) {
		for(RouteMeta meta : routes) {
			MatchResult result = meta.matches(request, subPath);
			if(result != null)
				return result;
		}

		return new MatchResult(false);
	}
	
}
