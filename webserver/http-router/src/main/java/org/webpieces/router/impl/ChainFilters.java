package org.webpieces.router.impl;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.dto.MethodMeta;
import org.webpieces.util.filters.Filter;
import org.webpieces.util.filters.Service;

public class ChainFilters {

	public static Service<MethodMeta, Action> addOnTop(Service<MethodMeta, Action> svc, Filter<MethodMeta, Action> filter) {
		return new ServiceFilterProxy(svc, filter);
	}
	
	private static class ServiceFilterProxy implements Service<MethodMeta, Action> {
		private Service<MethodMeta, Action> svc;
		private Filter<MethodMeta, Action> filter;

		public ServiceFilterProxy(Service<MethodMeta, Action> svc, Filter<MethodMeta, Action> filter) {
			this.svc = svc;
			this.filter = filter;
		}

		@Override
		public CompletableFuture<Action> invoke(MethodMeta meta) {
			Method method = meta.getMethod();
			CompletableFuture<Action> resp = filter.filter(meta, svc).thenApply((r) -> responseCheck(method, r));
			if(resp == null)
				throw new IllegalStateException("Filter returned null CompletableFuture<Action> which is not allowed="+filter.getClass()+" after being given request with controller method="+method);
			
			return resp;
		}
		
		private Action responseCheck(Method method, Action resp) {
			if(resp == null)
				throw new IllegalStateException("Filter returned CompletableFuture<Action> where the Action resolved to null which is not allowed="+filter.getClass()+" after being given request with controller method="+method);
			return resp;
		}

		@Override
		public String toString() {
			return filter +"->"+svc;
		}
	}
}
