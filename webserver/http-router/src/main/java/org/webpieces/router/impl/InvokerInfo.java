package org.webpieces.router.impl;

import org.webpieces.router.impl.model.RouteModuleInfo;
import org.webpieces.router.impl.routers.DynamicInfo;

public class InvokerInfo {

	private final DynamicRoute dynamicRoute;
	private final RouteModuleInfo routeModuleInfo;
	private DynamicInfo dynamicInfo;
	
	public InvokerInfo(DynamicRoute dynamicRoute, RouteModuleInfo routeModuleInfo, DynamicInfo dynamicInfo) {
		super();
		this.dynamicRoute = dynamicRoute;
		this.routeModuleInfo = routeModuleInfo;
		this.dynamicInfo = dynamicInfo;
	}

	public RouteModuleInfo getRouteModuleInfo() {
		return routeModuleInfo;
	}

	public DynamicInfo getDynamicInfo() {
		return dynamicInfo;
	}

	public DynamicRoute getDynamicRoute() {
		return dynamicRoute;
	}
	
}
