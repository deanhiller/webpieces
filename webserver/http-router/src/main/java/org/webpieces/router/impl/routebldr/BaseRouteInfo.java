package org.webpieces.router.impl.routebldr;

import java.util.List;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.impl.dto.RouteType;
import org.webpieces.router.impl.model.RouteModuleInfo;
import org.webpieces.util.filters.Service;

import com.google.inject.Injector;

public class BaseRouteInfo {

	private final Injector injector;
	private final List<FilterInfo<?>> filters;
	private final RouteType routeType;
	private final RouteInfo routeInfo;
	private Service<MethodMeta, Action> service;

	public BaseRouteInfo(Injector injector, RouteInfo routeInfo, Service<MethodMeta, Action> service, List<FilterInfo<?>> filters, RouteType routeType) {
		this.injector = injector;
		this.routeInfo = routeInfo;
		this.service = service;
		this.filters = filters;
		this.routeType = routeType;
	}



	public String getControllerMethodString() {
		return routeInfo.getControllerMethodString();
	}

	public List<FilterInfo<?>> getFilters() {
		return filters;
	}

	public RouteModuleInfo getRouteModuleInfo() {
		return routeInfo.getRouteModuleInfo();
	}
	
	public RouteInfo getRouteInfo() {
		return routeInfo;
	}

	public Injector getInjector() {
		return injector;
	}

	public RouteType getRouteType() {
		return routeType;
	}

	public Service<MethodMeta, Action> getService() {
		return service;
	}
	
}
