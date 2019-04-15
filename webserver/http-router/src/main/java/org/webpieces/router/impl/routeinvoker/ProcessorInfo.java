package org.webpieces.router.impl.routeinvoker;

import java.lang.reflect.Method;

import org.webpieces.router.impl.dto.RouteType;
import org.webpieces.router.impl.loader.LoadedController;

public class ProcessorInfo {

	private RouteType routeType;
	private Object controllerInstance;
	private Method method;
	
	public ProcessorInfo(RouteType routeType, LoadedController loadedController) {
		super();
		this.routeType = routeType;
		this.controllerInstance = loadedController.getControllerInstance();
		this.method = loadedController.getControllerMethod();
		
		if(controllerInstance == null)
			throw new IllegalStateException("Someone screwed up, as controllerInstance should not be null at this point, bug");
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
