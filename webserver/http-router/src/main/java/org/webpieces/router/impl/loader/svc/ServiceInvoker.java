package org.webpieces.router.impl.loader.svc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

import org.webpieces.router.api.controller.actions.Action;

public class ServiceInvoker {

	public Object invokeController(LoadedController2 meta, Object[] args) throws IllegalAccessException, InvocationTargetException {
		Method m = meta.getMethod();
		Object obj = meta.getControllerInstance();
		return m.invoke(obj, args);
	}

	public CompletableFuture<Action> createRuntimeFuture(Throwable e) {
		CompletableFuture<Action> future = new CompletableFuture<Action>();
		future.completeExceptionally(e);
		return future;
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

	public CompletableFuture<Action> invokeAndCoerce(LoadedController2 loadedController2, Object[] args) 
			throws IllegalAccessException, InvocationTargetException 
	{
		Object resp = invokeController(loadedController2, args);
		if(resp == null)
			throw new IllegalStateException("Your controller method returned null which is not allowed.  offending method="+loadedController2.getMethod());
		return coerceReturnValue(resp);
	}
}
