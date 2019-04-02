package org.webpieces.router.impl.ctx;

import java.lang.reflect.Method;

import org.webpieces.router.impl.dto.RouteType;

public class ProcessorInfo {

	private RouteType routeType;
	private Object controllerInstance;
	private Method method;
	
	public ProcessorInfo(RouteType routeType, Object controllerInstance, Method method) {
		super();
		this.routeType = routeType;
		this.controllerInstance = controllerInstance;
		this.method = method;
	}

	public RouteType getRouteType() {
		return routeType;
	}

	public Object getControllerInstance() {
		return controllerInstance;
	}

	public Method getMethod() {
		return method;
	}
}
