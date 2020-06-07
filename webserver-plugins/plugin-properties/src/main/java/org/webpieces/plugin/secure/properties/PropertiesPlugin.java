package org.webpieces.plugin.secure.properties;

import java.util.List;

import org.webpieces.router.api.plugins.Plugin;
import org.webpieces.router.api.routes.Routes;

import com.google.common.collect.Lists;
import com.google.inject.Module;

public class PropertiesPlugin implements Plugin {

	public static final String PLUGIN_PROPERTIES_KEY = "org.webpieces.plugin.properties";

	private PropertiesConfig config;

	public PropertiesPlugin(PropertiesConfig config) {
		super();
		this.config = config;
	}
	
	@Override
	public List<Module> getGuiceModules() {
		return Lists.newArrayList(new PropertiesModule(config));
	}

	@Override
	public List<Routes> getRouteModules() {
		return Lists.newArrayList(
			new PropertiesRoutes()
		);
	}

}
