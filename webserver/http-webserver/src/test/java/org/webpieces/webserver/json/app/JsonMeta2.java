package org.webpieces.webserver.json.app;

import java.util.List;

import org.webpieces.plugin.json.JacksonConfig;
import org.webpieces.plugin.json.JacksonPlugin;
import org.webpieces.router.api.plugins.Plugin;
import org.webpieces.router.api.routes.Routes;
import org.webpieces.router.api.routes.WebAppConfig;
import org.webpieces.router.api.routes.WebAppMeta;
import org.webpieces.webserver.EmptyModule;

import com.google.common.collect.Lists;
import com.google.inject.Module;

public class JsonMeta2 implements WebAppMeta {
	@Override
	public void initialize(WebAppConfig pluginConfig) {
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
		JacksonConfig config = new JacksonConfig("/json/.*");
		
		return Lists.<Plugin>newArrayList(
				new JacksonPlugin(config));
	}
}