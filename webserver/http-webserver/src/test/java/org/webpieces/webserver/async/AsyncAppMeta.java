package org.webpieces.webserver.async;

import java.util.List;

import org.webpieces.router.api.routing.RouteModule;
import org.webpieces.router.api.routing.WebAppMeta;

import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.google.inject.Module;

public class AsyncAppMeta implements WebAppMeta {
	public List<Module> getGuiceModules() {
		return Lists.newArrayList(new AsyncModule());
	}
	
	public List<RouteModule> getRouteModules() {
		return Lists.newArrayList(new AsyncRouteModule());
	}
	
	public static class AsyncModule implements Module {

		@Override
		public void configure(Binder binder) {
		}

	}
}