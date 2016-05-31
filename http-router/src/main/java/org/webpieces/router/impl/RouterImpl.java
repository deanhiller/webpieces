package org.webpieces.router.impl;

import java.io.File;
import java.util.Set;

import org.webpieces.router.api.HttpFilter;
import org.webpieces.router.api.Route;
import org.webpieces.router.api.RouteId;
import org.webpieces.router.api.Router;
import org.webpieces.router.api.dto.HttpMethod;

public class RouterImpl implements Router {

	private final RouteInfo info;
	private ReverseRoutes reverseRoutes;
	
	public RouterImpl(RouteInfo info, ReverseRoutes reverseRoutes) {
		this.info = info;
		this.reverseRoutes = reverseRoutes;
	}
	
	@Override
	public void addRoute(Route r, RouteId routeId) {
		info.addRoute(r);
		reverseRoutes.addRoute(routeId, r);
	}
	
	@Override
	public void addRoute(HttpMethod method, String path, String controllerMethod, RouteId routeId) {
		Route route = new RouteImpl(method, path, controllerMethod, routeId, false);
		addRoute(route, routeId);
	}

	@Override
	public void addRoute(Set<HttpMethod> methods, String path, String controllerMethod, RouteId routeId) {
		Route route = new RouteImpl(methods, path, controllerMethod, routeId, false);
		addRoute(route, routeId);
	}

	@Override
	public void addSecureRoute(HttpMethod method, String path, String controllerMethod, RouteId routeId) {
		Route route = new RouteImpl(method, path, controllerMethod, routeId, true);
		addRoute(route, routeId);
	}

	@Override
	public void addSecureRoute(Set<HttpMethod> methods, String path, String controllerMethod, RouteId routeId) {
		Route route = new RouteImpl(methods, path, controllerMethod, routeId, true);
		addRoute(route, routeId);
	}

	@Override
	public void addStaticGetRoute(String path, File f) {
	}

	@Override
	public void addFilter(String path, HttpFilter securityFilter) {
	}

	@Override
	public Router getScopedRouter(String path, boolean isSecure) {
		RouteInfo subInfo = info.addScope(path);
		return new RouterImpl(subInfo, reverseRoutes);
	}

	public RouteInfo getRouterInfo() {
		return info;
	}

	@Override
	public void setCatchAllRoute(String controllerMethod) {
		Route route = new RouteImpl(controllerMethod);
		info.setCatchAllRoute(route);
	}

	@Override
	public void setCatchAllRoute(Route r) {
		info.setCatchAllRoute(r);
	}
	
}
