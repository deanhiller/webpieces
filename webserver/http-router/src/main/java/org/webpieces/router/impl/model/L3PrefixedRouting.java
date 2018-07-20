package org.webpieces.router.impl.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.impl.Route;
import org.webpieces.router.impl.RouteLoader;
import org.webpieces.router.impl.RouteMeta;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public class L3PrefixedRouting {

	private static final Logger log = LoggerFactory.getLogger(L3PrefixedRouting.class);

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

		return null;
	}
	
	public List<RouteMeta> getRoutes() {
		return routes;
	}
	
	public Map<String, L3PrefixedRouting> getScopedRoutes() {
		return pathPrefixToInfo;
	}

	
	@Override
	public String toString() {
		return build("");
	}

	public void printRoutes(boolean isHttps, String tabSpaces) {
		//This is a pain but dynamically build up the html
		String routeHtml = build(tabSpaces);
		
		//print in warn so it's in red for anyone and to stderr IF they have debug enabled
		//it's kind of weird BUT great for tests
		if(!isHttps)
			log.warn("WARNING: The request is NOT https so perhaps your route is only accessible over https so modify your request" + routeHtml);
		else
			log.warn(routeHtml);
	}

	private String build(String spacing) {
		String text = "\n";
		
		Map<String, L3PrefixedRouting> scopedRoutes = getScopedRoutes();
		for(Map.Entry<String, L3PrefixedRouting> entry : scopedRoutes.entrySet()) {
			L3PrefixedRouting childRouting = entry.getValue();
			text += spacing+ "SCOPE:"+entry.getKey();
			text += childRouting.build(spacing +"    ");
		}
		
		List<RouteMeta> routes = getRoutes();
		for(RouteMeta route: routes) {
			Route rt = route.getRoute();
			String http = rt.isHttpsRoute() ? "https" : "http";
			text += spacing+pad(rt.getMethod(), 5)+":"+pad(http, 5)+" : "+rt.getFullPath()+"\n";
		}
		
		text+="\n";
		
		return text;
	}

	private String pad(String msg, int n) {
		int left = n-msg.length();
		if(left < 0)
			left = 0;
		
		for(int i = 0; i < left; i++) {
			msg += " ";
		}
		return msg;
	}	
}
