package org.webpieces.router.impl.services;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.util.futures.XFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.MDC;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.util.futures.FutureHelper;

@Singleton
public class ControllerInvoker {

	private static final Logger log = LoggerFactory.getLogger(ControllerInvoker.class);

	private FutureHelper futureUtil;

	@Inject
	public ControllerInvoker(FutureHelper futureUtil) {
		this.futureUtil = futureUtil;
	}
	
	public Object invokeController(LoadedController meta, Object[] args) throws IllegalAccessException, InvocationTargetException {
		Method m = meta.getControllerMethod();
		Object obj = meta.getControllerInstance();

		return m.invoke(obj, args);
	}

	@SuppressWarnings("unchecked")
	public XFuture<Action> coerceReturnValue(Object retVal) {
		if(retVal instanceof XFuture) {
			return (XFuture<Action>) retVal;
		} else if(retVal instanceof CompletableFuture) {
			//well, shit, the MDC is lost since they use CompletableFuture
			//this means anything stored in MDC for logging can't be logged
			log.warn("function returned CompletableFuture instead of XFuture, MDC for logging is lost since CompletableFutures do not transfer MDC");
			return XFuture.completedFuture(null).thenCompose((nothing) -> (CompletableFuture<Action>) retVal);
		} else {
			Action action = (Action) retVal;
			return XFuture.completedFuture(action);
		}
	}

	public XFuture<Action> invokeAndCoerce(LoadedController loadedController, Object[] args)
			throws IllegalAccessException, InvocationTargetException 
	{		
		Object resp = invokeController(loadedController, args);
		if(resp == null)
			throw new IllegalStateException("Your controller method returned null which is not allowed.  offending method="+loadedController.getControllerMethod());
		
		return coerceReturnValue(resp);
	}
	
}
