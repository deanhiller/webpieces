package org.playorm.util.api.serviceproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomServiceProxy implements InvocationHandler {

	private static final Logger log = Logger.getLogger(CustomServiceProxy.class.getName());
	protected Object service;

	private boolean isInjected;
	private boolean hasStartStop;

	private static String startMethodName = "start";

	private static String stopMethodName = "stop";

	public CustomServiceProxy(Object service) {
		this.hasStartStop = getClass().equals(CustomServiceProxy.class);
		isInjected = false;
		this.service = service;
	}
	
	public void setService(Object service) {
		if (service == null)
			throw new IllegalArgumentException(
					"use unsetService instead, can't pass in null");
		isInjected = true;
		this.service = service;
	}

	// the default service must be passed into us...
	public void unsetService(Object service) {
		if (service == null)
			throw new IllegalArgumentException(
					"cannot pass in null, service must"
							+ " be the default(not injected svc)");
		isInjected = false;
		this.service = service;
	}

	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		try {
			if (method.getName().equalsIgnoreCase("setService")) {
				this.setService(args[0]);
				return null;
			} else if (method.getName().equalsIgnoreCase("unsetService")) {
				this.unsetService(args[0]);
				return null;
			}

			if (service == null && hasStartStop)
				throw new RuntimeException("need set real service first");

			if (method.getName().equalsIgnoreCase(startMethodName)
					|| method.getName().equalsIgnoreCase(stopMethodName))
			{
				return callStartOrStop(proxy, method, args);
			}
			else
				return method.invoke(service, args);

		} catch (Exception e) {
			throw new RuntimeException("error ", e);
		}

	}

	private Object callStartOrStop(Object proxy, Method method, Object[] args) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if(isInjected)
			return null;
		
		//only invoke something if not dependency injected.
		//if something was dependency injected, the client
		//is responsible for it's lifecycle
		
		if(method.getName().equalsIgnoreCase(startMethodName)) {
			if(hasStartStop)
				return method.invoke(service, args);
			else {
				start();
				return null;
			}
		}
		
		//for stops, make sure we protect from exceptions...
		try {
			if(hasStartStop)
				return method.invoke(service, args);
			else
				stop();
		} catch(Throwable e) {
			log.log(Level.WARNING, "Exception in stop", e);
			return null;
		}
		return null;
	}

	public void start() {
	}
	public void stop() {
	}
}
