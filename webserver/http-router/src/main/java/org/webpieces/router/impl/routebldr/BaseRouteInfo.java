package org.webpieces.router.impl.routebldr;

import java.util.List;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.impl.dto.RouteType;
import org.webpieces.router.impl.model.RouteModuleInfo;
import org.webpieces.util.filters.Service;

import com.google.inject.Injector;

public class BaseRouteInfo {

	private final RouteType routeType;
	private final RouteInfo routeInfo;
	private final FilterCreationMeta filterChainCreationInfo;
	
	public BaseRouteInfo(Injector injector, RouteInfo routeInfo, Service<MethodMeta, Action> service, List<FilterInfo<?>> filters, RouteType routeType) {
		this.routeInfo = routeInfo;
		this.routeType = routeType;
		this.filterChainCreationInfo = new FilterCreationMeta(injector, filters, service);
	}



	public String getControllerMethodString() {
		return routeInfo.getControllerMethodString();
	}

	public RouteModuleInfo getRouteModuleInfo() {
		return routeInfo.getRouteModuleInfo();
	}
	
	public RouteInfo getRouteInfo() {
		return routeInfo;
	}

	public RouteType getRouteType() {
		return routeType;
	}

	public FilterCreationMeta getFilterChainCreationInfo() {
		return filterChainCreationInfo;
	}

}
