package org.webpieces.webserver.basic.app;

import java.util.List;
import java.util.Map;

import org.webpieces.router.api.routing.Plugin;
import org.webpieces.router.api.routing.RouteModule;
import org.webpieces.router.api.routing.WebAppMeta;
import org.webpieces.webserver.EmptyModule;

import com.google.common.collect.Lists;
import com.google.inject.Module;

public class BasicAppMeta implements WebAppMeta {
	@Override
	public void initialize(Map<String, String> props) {
	}
	@Override
    public List<Module> getGuiceModules() {
		return Lists.newArrayList(new EmptyModule());
	}
	
	@Override
    public List<RouteModule> getRouteModules() {
		return Lists.newArrayList(new BasicRouteModule());
	}
	@Override
	public List<Plugin> getPlugins() {
		return null;
	}
}