package org.webpieces.router.impl.loader;

import java.lang.reflect.Method;
import org.webpieces.util.futures.XFuture;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.routes.MethodMeta;
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
		public XFuture<Action> invoke(MethodMeta meta) {
			Method method = meta.getLoadedController().getControllerMethod();

			XFuture<Action> resp; 
			try {
				resp = filter.filter(meta, svc).thenApply((r) -> responseCheck(method, r));
			} catch(Throwable e) {
				resp = new XFuture<Action>();
				resp.completeExceptionally(e);
			}
			
			if(resp == null) {
				resp = new XFuture<Action>();
				resp.completeExceptionally(new IllegalStateException("Filter returned null XFuture<Action> which is not allowed="+filter.getClass()+" after being given request with controller method="+method));
			}
			
			return resp;
		}
		
		private Action responseCheck(Method method, Action resp) {
			if(resp == null)
				throw new IllegalStateException("Filter returned XFuture<Action> where the Action resolved to null which is not allowed="+filter.getClass()+" after being given request with controller method="+method);
			return resp;
		}

		@Override
		public String toString() {
			return filter +"->"+svc;
		}
	}
}
