package org.webpieces.router.impl.services;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.impl.loader.LoadedController;

public class ServiceInvoker {

	public Object invokeController(LoadedController meta, Object[] args) throws IllegalAccessException, InvocationTargetException {
		Method m = meta.getControllerMethod();
		Object obj = meta.getControllerInstance();
		return m.invoke(obj, args);
	}

	@SuppressWarnings("unchecked")
	public CompletableFuture<Action> coerceReturnValue(Object retVal) {
		if(retVal instanceof CompletableFuture) {
			return (CompletableFuture<Action>) retVal;
		} else {
			Action action = (Action) retVal;
			return CompletableFuture.completedFuture(action);
		}
	}

	public CompletableFuture<Action> invokeAndCoerce(LoadedController loadedController, Object[] args) 
			throws IllegalAccessException, InvocationTargetException 
	{
		Object resp = invokeController(loadedController, args);
		if(resp == null)
			throw new IllegalStateException("Your controller method returned null which is not allowed.  offending method="+loadedController.getControllerMethod());
		
		return coerceReturnValue(resp);
	}
}
