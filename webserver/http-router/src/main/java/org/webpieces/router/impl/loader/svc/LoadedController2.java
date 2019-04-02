package org.webpieces.router.impl.loader.svc;

import java.lang.reflect.Method;

public class LoadedController2 {

	private Method method;
	private Object controllerInstance;

	public LoadedController2(Object controllerInstance, Method method) {
		super();
		this.method = method;
		this.controllerInstance = controllerInstance;
	}

	public Method getMethod() {
		return method;
	}

	public Object getControllerInstance() {
		return controllerInstance;
	}

}
