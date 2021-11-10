package org.webpieces.router.impl.services;

import java.lang.reflect.InvocationTargetException;
import org.webpieces.util.futures.XFuture;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.util.filters.Service;
import org.webpieces.util.futures.FutureHelper;

/**
 * NOTE: This is for InternalError AND NotFound BUT if they deviate separate so we stay with
 * composition which is more flexible/extensible and maintainble
 */
public class SvcProxyFixedRoutes implements Service<MethodMeta, Action> {

	private ControllerInvoker invoker;
	private FutureHelper futureUtil;

	public SvcProxyFixedRoutes(ControllerInvoker invoker, FutureHelper futureUtil) {
		this.invoker = invoker;
		this.futureUtil = futureUtil;
	}
	
	@Override
	public XFuture<Action> invoke(MethodMeta meta) {
		return futureUtil.syncToAsyncException(() -> invokeMethod(meta));
	}
	
	private XFuture<Action> invokeMethod(MethodMeta meta) 
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return invoker.invokeAndCoerce(meta.getLoadedController(), new Object[0]);
	}

}
