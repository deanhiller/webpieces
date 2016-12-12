package org.webpieces.plugins.hibernate.app;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.webpieces.router.api.routing.RouteModule;
import org.webpieces.router.api.routing.WebAppMeta;

import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.google.inject.Module;

public class HibernateAppMeta implements WebAppMeta {

    @Override
    public List<Module> getGuiceModules() {
		return Lists.newArrayList(new AppModule());
	}
	
	@Override
    public List<RouteModule> getRouteModules() {
		return Lists.newArrayList(new HibernateTestRouteModule());
	}
	
	private class AppModule implements Module {
		@Override
		public void configure(Binder binder) {
			ExecutorService executor = Executors.newSingleThreadExecutor();
			binder.bind(Executor.class).toInstance(executor);
		}
	}
}