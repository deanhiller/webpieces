package org.webpieces.webserver.async.app;

import java.util.List;
import java.util.Map;

import org.webpieces.router.api.plugins.Plugin;
import org.webpieces.router.api.routes.Routes;
import org.webpieces.router.api.routes.WebAppMeta;
import org.webpieces.webserver.EmptyModule;

import com.google.common.collect.Lists;
import com.google.inject.Module;

public class AsyncAppMeta implements WebAppMeta {
	@Override
	public void initialize(Map<String, String> props) {
	}
	
	@Override
    public List<Module> getGuiceModules() {
		return Lists.newArrayList(new EmptyModule());
	}
	
	@Override
    public List<Routes> getRouteModules() {
		return Lists.newArrayList(new AsyncRoutes());
	}
	
	@Override
	public List<Plugin> getPlugins() {
		return null;
	}
}