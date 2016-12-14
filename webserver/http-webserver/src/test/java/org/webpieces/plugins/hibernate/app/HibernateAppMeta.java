package org.webpieces.plugins.hibernate.app;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.inject.Binder;
import org.webpieces.plugins.hibernate.HibernatePlugin;
import org.webpieces.plugins.hsqldb.H2DbPlugin;
import org.webpieces.router.api.routing.Plugin;
import org.webpieces.router.api.routing.RouteModule;
import org.webpieces.router.api.routing.WebAppMeta;

import com.google.common.collect.Lists;
import com.google.inject.Module;

public class HibernateAppMeta implements WebAppMeta {
	public static final String PERSISTENCE_TEST_UNIT = "webpieces-persistence";

	@Override
	public void initialize(Map<String, String> props) {
	}
	
    @Override
    public List<Module> getGuiceModules() {
		return Lists.newArrayList(new AppModule());
	}
	
	@Override
    public List<RouteModule> getRouteModules() {
		return Lists.newArrayList(new HibernateTestRouteModule());
	}
	
	@Override
	public List<Plugin> getPlugins() {
		return Lists.<Plugin>newArrayList(
				new HibernatePlugin(PERSISTENCE_TEST_UNIT));
	}

	private class AppModule implements Module {
		@Override
		public void configure(Binder binder) {
			ExecutorService executor = Executors.newSingleThreadExecutor();
			binder.bind(Executor.class).toInstance(executor);
		}
	}
}