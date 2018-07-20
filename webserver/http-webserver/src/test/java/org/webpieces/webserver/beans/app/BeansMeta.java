package org.webpieces.webserver.beans.app;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.webpieces.router.api.SimpleStorage;
import org.webpieces.router.api.routing.Plugin;
import org.webpieces.router.api.routing.Routes;
import org.webpieces.router.api.routing.WebAppMeta;
import org.webpieces.webserver.EmptyStorage;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Module;

public class BeansMeta implements WebAppMeta {
	@Override
	public void initialize(Map<String, String> props) {
	}
	@Override
    public List<Module> getGuiceModules() {
		return Lists.newArrayList(new AbstractModule() {
			@Override
			protected void configure() {
				bind(Executor.class).toInstance(Executors.newFixedThreadPool(1));
				bind(SimpleStorage.class).toInstance(new EmptyStorage());
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