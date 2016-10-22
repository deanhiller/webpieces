package org.webpieces.plugins.hibernate.app;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.webpieces.plugins.hibernate.HibernateModule;
import org.webpieces.plugins.hibernate.HibernateRouteModule;
import org.webpieces.plugins.hsqldb.H2DbModule;
import org.webpieces.router.api.routing.RouteModule;
import org.webpieces.router.api.routing.WebAppMeta;

import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.google.inject.Module;

public class HibernateAppMeta implements WebAppMeta {
	public List<Module> getGuiceModules() {
		return Lists.newArrayList(
				new H2DbModule(),
				new HibernateModule("fortest"),
				new AppModule()
				);
	}
	
	public List<RouteModule> getRouteModules() {
		return Lists.newArrayList(
				new HibernateRouteModule(),
				new HibernateTestRouteModule()
				);
	}
	
	private class AppModule implements Module {
		@Override
		public void configure(Binder binder) {
			ExecutorService executor = Executors.newSingleThreadExecutor();
			binder.bind(Executor.class).toInstance(executor);
		}
	}
}