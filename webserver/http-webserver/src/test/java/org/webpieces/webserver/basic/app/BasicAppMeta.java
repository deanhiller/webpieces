package org.webpieces.webserver.basic.app;

import com.google.common.collect.Lists;
import com.google.inject.Module;
import org.webpieces.router.api.plugins.Plugin;
import org.webpieces.router.api.routes.Routes;
import org.webpieces.router.api.routes.WebAppConfig;
import org.webpieces.router.api.routes.WebAppMeta;
import org.webpieces.webserver.EmptyModule;

import java.util.List;

public class BasicAppMeta implements WebAppMeta {
	@Override
	public void initialize(WebAppConfig pluginConfig) {
	}
	@Override
    public List<Module> getGuiceModules() {
		return Lists.newArrayList(new EmptyModule());
	}
	
	@Override
    public List<Routes> getRouteModules() {
		return Lists.newArrayList(
				new BasicRoutes(),
				new SomeScopedRoutes()
				);
	}
	@Override
	public List<Plugin> getPlugins() {
		return null;
	}
}