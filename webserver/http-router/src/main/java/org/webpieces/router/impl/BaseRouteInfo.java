package org.webpieces.router.impl;

import java.util.List;

import org.webpieces.router.impl.dto.RouteType;
import org.webpieces.router.impl.model.RouteModuleInfo;

import com.google.inject.Injector;

public class BaseRouteInfo {

	private final Injector injector;
	private final RouteModuleInfo routeModuleInfo;
	private final String controllerMethodString;
	private final List<FilterInfo<?>> filters;
	private final RouteType routeType;

	public BaseRouteInfo(Injector injector, RouteModuleInfo routeModuleInfo, String controllerMethodString, List<FilterInfo<?>> filters, RouteType routeType) {
		this.injector = injector;
		this.routeModuleInfo = routeModuleInfo;
		this.controllerMethodString = controllerMethodString;
		this.filters = filters;
		this.routeType = routeType;
	}

	public String getControllerMethodString() {
		return controllerMethodString;
	}

	public List<FilterInfo<?>> getFilters() {
		return filters;
	}

	public RouteModuleInfo getRouteModuleInfo() {
		return routeModuleInfo;
	}

	public Injector getInjector() {
		return injector;
	}

	public RouteType getRouteType() {
		return routeType;
	}
	
}
