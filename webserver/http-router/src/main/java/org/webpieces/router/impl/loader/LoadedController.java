package org.webpieces.router.impl.loader;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

public class LoadedController {

	private final Object controllerInstance;
	private final Method controllerMethod;
	private final List<String> paramNames;
	private final Parameter[] parameters;

	public LoadedController(Object controllerInstance, Method controllerMethod, Parameter[] parameters, List<String> paramNames) {
		this.controllerInstance = controllerInstance;
		this.controllerMethod = controllerMethod;
		this.parameters = parameters;
		this.paramNames = paramNames;
	}

	public Object getControllerInstance() {
		return controllerInstance;
	}

	public Method getControllerMethod() {
		return controllerMethod;
	}

	public List<String> getParamNames() {
		return paramNames;
	}

	public Parameter[] getParameters() {
		return parameters;
	}
	
	

}
