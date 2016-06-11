package org.webpieces.router.impl;

import java.lang.reflect.Method;
import java.util.List;

public class RouteMeta {

	private final Route route;
	private Object controllerInstance;
	private Method method;
	private List<String> methodParamNames;

	public RouteMeta(Route r) {
		this.route = r;
	}

	public Route getRoute() {
		return route;
	}

	public Object getControllerInstance() {
		return controllerInstance;
	}

	public Method getMethod() {
		return method;
	}

	public void setControllerInstance(Object controllerInstance) {
		this.controllerInstance = controllerInstance;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public void setMethodParamNames(List<String> paramNames) {
		this.methodParamNames = paramNames;
	}
	
	
	
}
