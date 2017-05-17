package org.webpieces.webserver.dev.app;

import java.util.List;
import java.util.Map;

import org.webpieces.plugins.json.JacksonPlugin;
import org.webpieces.router.api.routing.Plugin;
import org.webpieces.router.api.routing.Routes;
import org.webpieces.router.api.routing.WebAppMeta;
import org.webpieces.webserver.EmptyModule;
import org.webpieces.webserver.json.app.TestCatchAllFilter;

import com.google.common.collect.Lists;
import com.google.inject.Module;

public class DevMeta implements WebAppMeta {
	@Override
	public void initialize(Map<String, String> props) {
	}

	@Override
	public List<Plugin> getPlugins() {
		return Lists.<Plugin>newArrayList(
				new JacksonPlugin("/json/.*", TestCatchAllFilter.class));
	}
	
	public List<Module> getGuiceModules() {
		return Lists.newArrayList(new EmptyModule());
	}
	
	public List<Routes> getRouteModules() {
		return Lists.newArrayList(new DevRoutes());
	}
	
}