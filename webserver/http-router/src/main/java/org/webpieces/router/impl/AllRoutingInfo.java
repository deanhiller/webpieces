package org.webpieces.router.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.webpieces.router.api.dto.RouterRequest;

public class AllRoutingInfo {

	private final Map<String, AllRoutingInfo> pathPrefixToInfo = new HashMap<>();
	private List<RouteMeta> routes = new ArrayList<>();
	private RouteMeta pageNotFoundRoute;
	private RouteMeta internalSvrErrorRoute;
	
	public void addRoute(RouteMeta r) {
		this.routes.add(r);
	}
	
	public void setPageNotFoundRoute(RouteMeta r) {
		if(pageNotFoundRoute != null)
			throw new IllegalStateException("You cannot set the PageNotFoundRoute twice");
		this.pageNotFoundRoute = r;
	}
	
	public void setInternalSvrErrorRoute(RouteMeta r) {
		if(internalSvrErrorRoute != null)
			throw new IllegalStateException("You cannot set the InternalSvrErrorRoute twice");
		this.internalSvrErrorRoute = r;
	}
	
	public AllRoutingInfo addScope(String path) {
		AllRoutingInfo routerInfo = pathPrefixToInfo.get(path);
		if(routerInfo == null) {
			routerInfo = new AllRoutingInfo();
			pathPrefixToInfo.put(path, routerInfo);
		}
		return routerInfo;
	}

	public MatchResult fetchRoute(RouterRequest request, String path) {
		if(!path.startsWith("/"))
			throw new IllegalArgumentException("path must start with /");

		String prefix = path;
		int index = path.indexOf("/", 1);
		if(index == 1) {
			throw new IllegalArgumentException("path cannot start with //");
		} else if(index > 1) {
			prefix = path.substring(0, index);
		}

		AllRoutingInfo routeInfo = pathPrefixToInfo.get(prefix);
		if(routeInfo != null) {
			String newRelativePath = path.substring(index, path.length());
			MatchResult route = routeInfo.fetchRoute(request, newRelativePath);
			if(route != null)
				return route;
		}

		for(RouteMeta meta : routes) {
			MatchResult result = meta.matches(request, path);
			if(result != null)
				return result;
		}

		return new MatchResult(pageNotFoundRoute);
	}

	public boolean isPageNotFoundRouteSet() {
		return pageNotFoundRoute != null;
	}

	public RouteMeta getPageNotfoundRoute() {
		return pageNotFoundRoute;
	}

	public RouteMeta getInternalErrorRoute() {
		return internalSvrErrorRoute;
	}
	
	@Override
	public String toString() {
		return "RouteInfo state...\npageNotFoundRoute=\n*****"
				+ pageNotFoundRoute + "\n*****\nroutes=" + routes + "\n\npathPrefixToInfo=" + pathPrefixToInfo + "]";
	}

	public boolean isInternalSvrErrorRouteSet() {
		return internalSvrErrorRoute != null;
	}
	
}