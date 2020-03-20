package org.webpieces.plugins.hibernate.app;

import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.name.Names;
import org.webpieces.nio.api.SSLConfiguration;
import org.webpieces.nio.api.SSLEngineFactory;
import org.webpieces.plugins.hibernate.HibernatePlugin;
import org.webpieces.plugins.hibernate.app.ajax.AjaxHibernateCrudRoutes;
import org.webpieces.router.api.extensions.SimpleStorage;
import org.webpieces.router.api.plugins.Plugin;
import org.webpieces.router.api.routes.Routes;
import org.webpieces.router.api.routes.WebAppConfig;
import org.webpieces.router.api.routes.WebAppMeta;
import org.webpieces.webserver.EmptyStorage;
import org.webpieces.webserver.SSLEngineFactoryWebServerTesting;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HibernateAppMeta implements WebAppMeta {
	public static final String PERSISTENCE_TEST_UNIT = "webpieces-persistence";
	private WebAppConfig pluginConfig;

	@Override
	public void initialize(WebAppConfig pluginConfig) {
		this.pluginConfig = pluginConfig;
	}
	
    @Override
    public List<Module> getGuiceModules() {
		return Lists.newArrayList(new AppModule());
	}
	
	@Override
    public List<Routes> getRouteModules() {
		return Lists.newArrayList(
				new HibernateTestRoutes(),
				new AjaxHibernateCrudRoutes());
	}
	
	@Override
	public List<Plugin> getPlugins() {
		return Lists.<Plugin>newArrayList(
				new HibernatePlugin(pluginConfig.getCmdLineArguments()));
	}

	private class AppModule implements Module {
		@Override
		public void configure(Binder binder) {
			ExecutorService executor = Executors.newSingleThreadExecutor();
			binder.bind(Executor.class).toInstance(executor);
			
			binder.bind(SimpleStorage.class).toInstance(new EmptyStorage());
			
			binder.bind(SSLEngineFactory.class).to(SSLEngineFactoryWebServerTesting.class);
			binder.bind(SSLEngineFactory.class).annotatedWith(Names.named(SSLConfiguration.BACKEND_SSL)).to(SSLEngineFactoryWebServerTesting.class);
		}
	}
}