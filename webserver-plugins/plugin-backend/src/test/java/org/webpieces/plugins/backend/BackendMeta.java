package org.webpieces.plugins.backend;

import java.util.List;
import java.util.Map;

import org.webpieces.plugins.fortesting.EmptyModule;
import org.webpieces.plugins.fortesting.FillerRoutes;
import org.webpieces.router.api.plugins.Plugin;
import org.webpieces.router.api.routes.Routes;
import org.webpieces.router.api.routes.WebAppMeta;

import com.google.common.collect.Lists;
import com.google.inject.Module;

public class BackendMeta implements WebAppMeta {
	@Override
	public void initialize(Map<String, String> props) {
	}
	@Override
    public List<Module> getGuiceModules() {
		return Lists.newArrayList(new EmptyModule());
	}
	
	@Override
    public List<Routes> getRouteModules() {
		return Lists.newArrayList(new FillerRoutes());
	}
	@Override
	public List<Plugin> getPlugins() {
		return Lists.newArrayList(
				new BackendPlugin(new BackendConfig())
		);
	}
}
