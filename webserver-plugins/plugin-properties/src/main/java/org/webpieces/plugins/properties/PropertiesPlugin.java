package org.webpieces.plugins.properties;

import java.util.List;

import org.webpieces.router.api.routing.Plugin;
import org.webpieces.router.api.routing.Routes;

import com.google.common.collect.Lists;
import com.google.inject.Module;

public class PropertiesPlugin implements Plugin {

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
