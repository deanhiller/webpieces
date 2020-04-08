package org.webpieces.router.api.error.dev;

import java.util.List;

import org.webpieces.ctx.api.ApplicationContext;
import org.webpieces.router.api.extensions.SimpleStorage;
import org.webpieces.router.api.plugins.Plugin;
import org.webpieces.router.api.routes.WebAppConfig;
import org.webpieces.router.api.routes.Routes;
import org.webpieces.router.api.routes.WebAppMeta;
import org.webpieces.router.api.simplesvr.EmptyStorage;
import org.webpieces.router.impl.ApplicationContextImpl;

import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.google.inject.Module;

public class NoMethodRouterModules implements WebAppMeta {

	@Override
	public void initialize(WebAppConfig pluginConfig) {
	}
	
	public List<Module> getGuiceModules() {
		return Lists.newArrayList(new Module() {
			@Override
			public void configure(Binder binder) {
				binder.bind(SimpleStorage.class).toInstance(new EmptyStorage());
				binder.bind(ApplicationContext.class).to(ApplicationContextImpl.class).asEagerSingleton();;
			}});
	}
	
	public List<Routes> getRouteModules() {
		return Lists.newArrayList(new NoMethodRoutes());
	}

	@Override
	public List<Plugin> getPlugins() {
		return null;
	}
}
