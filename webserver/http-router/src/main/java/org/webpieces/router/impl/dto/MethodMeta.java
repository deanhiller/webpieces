package org.webpieces.router.impl.dto;

import java.lang.reflect.Method;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.extensions.BodyContentBinder;
import org.webpieces.router.impl.Route;

public class MethodMeta {

	private Object controllerInstance;
	private Method method;
	private RequestContext ctx;
	private Route route;
	private BodyContentBinder bodyContentBinder;

	public MethodMeta(
			Object controllerInstance, Method method, RequestContext ctx, Route route, BodyContentBinder bodyContentBinder) {
		this.controllerInstance = controllerInstance;
		this.method = method;
		this.ctx = ctx;
		this.route = route;
		this.bodyContentBinder = bodyContentBinder;
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

	public BodyContentBinder getBodyContentBinder() {
		return bodyContentBinder;
	}

}
