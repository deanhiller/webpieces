package org.webpieces.webserver.beans.app;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.name.Names;
import org.webpieces.nio.api.SSLConfiguration;
import org.webpieces.nio.api.SSLEngineFactory;
import org.webpieces.router.api.extensions.SimpleStorage;
import org.webpieces.router.api.plugins.Plugin;
import org.webpieces.router.api.routes.Routes;
import org.webpieces.router.api.routes.WebAppConfig;
import org.webpieces.router.api.routes.WebAppMeta;
import org.webpieces.webserver.EmptyStorage;
import org.webpieces.webserver.SSLEngineFactoryWebServerTesting;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BeansMeta implements WebAppMeta {
	@Override
	public void initialize(WebAppConfig pluginConfig) {
	}
	@Override
    public List<Module> getGuiceModules() {
		return Lists.newArrayList(new AbstractModule() {
			@Override
			protected void configure() {
				bind(Executor.class).toInstance(Executors.newFixedThreadPool(1));
				bind(SimpleStorage.class).toInstance(new EmptyStorage());
				
				bind(SSLEngineFactory.class).to(SSLEngineFactoryWebServerTesting.class);
				bind(SSLEngineFactory.class).annotatedWith(Names.named(SSLConfiguration.BACKEND_SSL)).to(SSLEngineFactoryWebServerTesting.class);
			}
		});
	}
	
	@Override
    public List<Routes> getRouteModules() {
		return Lists.newArrayList(new BeansRoutes());
	}
	@Override
	public List<Plugin> getPlugins() {
		return null;
	}
}