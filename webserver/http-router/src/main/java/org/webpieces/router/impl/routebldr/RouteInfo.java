package org.webpieces.router.impl.routebldr;

import org.webpieces.router.impl.model.RouteModuleInfo;

public class RouteInfo {

	private final RouteModuleInfo routeModuleInfo;
	private final String controllerMethodString;

	public RouteInfo(RouteModuleInfo routeModuleInfo, String controllerMethodString) {
		super();
		this.routeModuleInfo = routeModuleInfo;
		this.controllerMethodString = controllerMethodString;
	}

	public RouteModuleInfo getRouteModuleInfo() {
		return routeModuleInfo;
	}

	public String getControllerMethodString() {
		return controllerMethodString;
	}

	@Override
	public String toString() {
		return "RouteInfo [controllerMethodString=" + controllerMethodString + "]";
	}

}
