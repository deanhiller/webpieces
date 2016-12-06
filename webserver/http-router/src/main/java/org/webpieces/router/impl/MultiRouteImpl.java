package org.webpieces.router.impl;

import java.util.List;
import java.util.regex.Matcher;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.dto.RouteType;
import org.webpieces.router.api.routing.RouteId;

public class MultiRouteImpl implements Route {

	private String controllerMethodString;

	public MultiRouteImpl(HttpMethod method, List<String> paths, String controllerMethod, RouteId routeId, boolean isSecure) {
		this.controllerMethodString = controllerMethod;
	}

	@Override
	public String getPath() {
		return null;
	}

	@Override
	public boolean matchesMethod(HttpMethod method) {
		return false;
	}

	@Override
	public Matcher matches(RouterRequest request, String subPath) {
		return null;
	}

	@Override
	public String getControllerMethodString() {
		return controllerMethodString;
	}

	@Override
	public List<String> getPathParamNames() {
		return null;
	}

	@Override
	public RouteType getRouteType() {
		return RouteType.MULTI_ROUTE;
	}

	@Override
	public boolean isPostOnly() {
		return false;
	}

	@Override
	public boolean isCheckSecureToken() {
		return false;
	}

	@Override
	public boolean isHttpsRoute() {
		return false;
	}

}
