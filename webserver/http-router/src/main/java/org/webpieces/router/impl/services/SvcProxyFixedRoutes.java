package org.webpieces.router.impl.services;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletableFuture;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.util.filters.ExceptionUtil;
import org.webpieces.util.filters.Service;

/**
 * NOTE: This is for InternalError AND NotFound BUT if they deviate separate so we stay with
 * composition which is more flexible/extensible and maintainble
 */
public class SvcProxyFixedRoutes implements Service<MethodMeta, Action> {

	private ServiceInvoker invoker;

	public SvcProxyFixedRoutes(ServiceInvoker invoker) {
		this.invoker = invoker;
	}
	
	@Override
	public CompletableFuture<Action> invoke(MethodMeta meta) {
		return ExceptionUtil.wrap(() -> invokeMethod(meta));
	}
	
	private CompletableFuture<Action> invokeMethod(MethodMeta meta) 
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return invoker.invokeAndCoerce(meta.getLoadedController2(), new Object[0]);
	}

}
