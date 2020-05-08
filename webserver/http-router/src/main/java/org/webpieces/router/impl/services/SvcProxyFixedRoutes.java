package org.webpieces.router.impl.services;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletableFuture;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.util.filters.Service;
import org.webpieces.util.futures.FutureHelper;

/**
 * NOTE: This is for InternalError AND NotFound BUT if they deviate separate so we stay with
 * composition which is more flexible/extensible and maintainble
 */
public class SvcProxyFixedRoutes implements Service<MethodMeta, Action> {

	private ServiceInvoker invoker;
	private FutureHelper futureUtil;

	public SvcProxyFixedRoutes(ServiceInvoker invoker, FutureHelper futureUtil) {
		this.invoker = invoker;
		this.futureUtil = futureUtil;
	}
	
	@Override
	public CompletableFuture<Action> invoke(MethodMeta meta) {
		return futureUtil.syncToAsyncException(() -> invokeMethod(meta));
	}
	
	private CompletableFuture<Action> invokeMethod(MethodMeta meta) 
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return invoker.invokeAndCoerce(meta.getLoadedController2(), new Object[0]);
	}

}
