package org.webpieces.webserver.json.app;

import java.util.List;
import java.util.Map;

import org.webpieces.plugins.json.JacksonConfig;
import org.webpieces.plugins.json.JacksonPlugin;
import org.webpieces.router.api.plugins.Plugin;
import org.webpieces.router.api.routes.Routes;
import org.webpieces.router.api.routes.WebAppMeta;
import org.webpieces.webserver.EmptyModule;

import com.google.common.collect.Lists;
import com.google.inject.Module;

public class JsonMeta implements WebAppMeta {
	@Override
	public void initialize(Map<String, String> props) {
	}
	@Override
    public List<Module> getGuiceModules() {
		return Lists.newArrayList(new EmptyModule());
	}
	
	@Override
    public List<Routes> getRouteModules() {
		return Lists.newArrayList(new JsonRoutes());
	}
	@Override
	public List<Plugin> getPlugins() {
		return Lists.<Plugin>newArrayList(
				new JacksonPlugin(new JacksonConfig("/json/.*", TestCatchAllFilter.class)));
	}
}