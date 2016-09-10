package org.webpieces.router.impl.loader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.dto.MethodMeta;
import org.webpieces.router.impl.InvokeException;
import org.webpieces.util.filters.Service;

public class ServiceProxy extends Service<MethodMeta, Action> {

	@Override
	public CompletableFuture<Action> invoke(MethodMeta meta) {
		try {
			return invokeMethod(meta);
		} catch(InvocationTargetException e) {
			Throwable cause = e.getCause();
			if(cause instanceof RuntimeException) {
				throw (RuntimeException)cause;
			} else {
				throw new InvokeException(e);
			}
		} catch (IllegalAccessException | IllegalArgumentException e) {
			throw new InvokeException(e);
		}			
	}
	
	@SuppressWarnings("unchecked")
	private CompletableFuture<Action> invokeMethod(MethodMeta meta) 
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		Method m = meta.getMethod();
		Object obj = meta.getControllerInstance();
		Object[] arguments = meta.getArguments();
		
		Object retVal = m.invoke(obj, arguments);
		if(retVal instanceof CompletableFuture) {
			return (CompletableFuture<Action>) retVal;
		} else {
			Action action = (Action) retVal;
			return CompletableFuture.completedFuture(action);
		}
	}
}
