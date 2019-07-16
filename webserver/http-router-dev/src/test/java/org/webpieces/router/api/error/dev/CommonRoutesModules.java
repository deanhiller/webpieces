package org.webpieces.router.api.error.dev;

import java.util.List;

import org.webpieces.router.api.extensions.SimpleStorage;
import org.webpieces.router.api.plugins.Plugin;
import org.webpieces.router.api.routes.WebAppConfig;
import org.webpieces.router.api.routes.Routes;
import org.webpieces.router.api.routes.WebAppMeta;
import org.webpieces.router.api.simplesvr.EmptyStorage;

import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.google.inject.Module;

public class CommonRoutesModules implements WebAppMeta {

	@Override
	public void initialize(WebAppConfig pluginConfig) {
	}
	
	public List<Module> getGuiceModules() {
		return Lists.newArrayList(new Module() {
			@Override
			public void configure(Binder binder) {
				binder.bind(SimpleStorage.class).toInstance(new EmptyStorage());
			}});
	}
	
	public List<Routes> getRouteModules() {
		return Lists.newArrayList(new CommonRoutes());
	}

	@Override
	public List<Plugin> getPlugins() {
		return null;
	}
}
