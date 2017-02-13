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

	public L3PrefixedRouting getScopedRouter(String path) {
		L3PrefixedRouting r = pathPrefixToInfo.get(path);
		if(r == null) {
			r = new L3PrefixedRouting();
			pathPrefixToInfo.put(path, r);
		}
		return r;
	}

	public void addRoute(RouteMeta meta) {
		routes.add(meta);
	}


	public MatchResult fetchRoute(RouterRequest request, String subPath, RouteMeta pageNotFoundRoute) {
		MatchResult result = fetchRouteImpl(request, subPath);
		if(result.isNotFound())
			return new MatchResult(pageNotFoundRoute);
		return result;
	}
	
	private MatchResult fetchRouteImpl(RouterRequest request, String subPath) {
		if(!subPath.startsWith("/"))
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
			String newRelativePath = subPath.substring(index, subPath.length());
			MatchResult route = routeInfo.fetchRouteImpl(request, newRelativePath);
			if(route != null)
				return route;
		}

		for(RouteMeta meta : routes) {
			MatchResult result = meta.matches(request, subPath);
			if(result != null)
				return result;
		}

		return new MatchResult(true);
	}
	
}
