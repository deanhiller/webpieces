package org.webpieces.router.api.dto;

import java.lang.reflect.Method;

import org.webpieces.ctx.api.RequestContext;

public class MethodMeta {

	private Object controllerInstance;
	private Method method;
	private Object[] arguments;
	private RequestContext ctx;

	public MethodMeta(Object controllerInstance, Method method, Object[] arguments, RequestContext ctx) {
		this.controllerInstance = controllerInstance;
		this.method = method;
		this.arguments = arguments;
		this.ctx = ctx;
	}

	public Object getControllerInstance() {
		return controllerInstance;
	}

	public Method getMethod() {
		return method;
	}

	public Object[] getArguments() {
		return arguments;
	}

	public RequestContext getCtx() {
		return ctx;
	}

}
