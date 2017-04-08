package org.webpieces.router.api.dto;

import java.lang.reflect.Method;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.impl.Route;

public class MethodMeta {

	private Object controllerInstance;
	private Method method;
	private RequestContext ctx;
	private Route route;

	public MethodMeta(
			Object controllerInstance, Method method, RequestContext ctx, Route route) {
		this.controllerInstance = controllerInstance;
		this.method = method;
		this.ctx = ctx;
		this.route = route;
	}

	public Object getControllerInstance() {
		return controllerInstance;
	}

	public Method getMethod() {
		return method;
	}

	public RequestContext getCtx() {
		return ctx;
	}

	public Route getRoute() {
		return route;
	}

}
