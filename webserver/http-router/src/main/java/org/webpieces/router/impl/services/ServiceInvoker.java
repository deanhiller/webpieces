package org.webpieces.router.impl.services;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

import org.slf4j.MDC;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.util.futures.ExceptionUtil;

public class ServiceInvoker {

	public Object invokeController(LoadedController meta, Object[] args) throws IllegalAccessException, InvocationTargetException {
		Method m = meta.getControllerMethod();
		Object obj = meta.getControllerInstance();
		return m.invoke(obj, args);
	}

	@SuppressWarnings("unchecked")
	public CompletableFuture<Action> coerceReturnValue(Object retVal) {
		if(retVal instanceof CompletableFuture) {
			//SPECIAL case...cache MDC to make exist over threads so if the future 'completes' on another thread(very typical)
			//then the function in the finallyBlock below will run on that thread..
			Map<String, String> mdcCopy = MDC.getCopyOfContextMap();
			
			//special case, IF the controller is returning a future, then later when he responds, the MDC is LOST BECAUSE
			//Java will not implement scalas Local.scala(like a ThreadLocal but for futures so you can have state follow
			//the request.  This means, logging breaks over some threads in the controller and we CANNOT fix that!!!
			CompletableFuture<Action> future = (CompletableFuture<Action>) retVal;
			return ExceptionUtil.finallyBlock( () -> future, () -> {
				for(Entry<String, String> entry : mdcCopy.entrySet()) {
					MDC.put(entry.getKey(), entry.getValue());
				}
			});
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
