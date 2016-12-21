package org.webpieces.router.api.dto;

import java.lang.reflect.Method;

import org.webpieces.ctx.api.RequestContext;

public class MethodMeta {

	private Object controllerInstance;
	private Method method;
	private RequestContext ctx;

	public MethodMeta(
			Object controllerInstance, Method method, RequestContext ctx) {
		this.controllerInstance = controllerInstance;
		this.method = method;
		this.ctx = ctx;
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

}
