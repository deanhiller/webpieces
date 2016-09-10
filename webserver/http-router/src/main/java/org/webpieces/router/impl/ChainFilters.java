package org.webpieces.router.impl;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.dto.MethodMeta;
import org.webpieces.util.filters.Filter;
import org.webpieces.util.filters.Service;

public class ChainFilters {

	public static Service<MethodMeta, Action> addOnTop(Service<MethodMeta, Action> svc, Filter<MethodMeta, Action> filter) {
		return new Service<MethodMeta, Action>() {
			@Override
			public CompletableFuture<Action> invoke(MethodMeta meta) {
				Method method = meta.getMethod();
				CompletableFuture<Action> resp = filter.filter(meta, svc);
				if(resp == null)
					throw new IllegalStateException("Filter returned null which is not allowed="+filter.getClass()+" after being given request with controller method="+method);
				
				return resp;
			}
		};
	}
}
