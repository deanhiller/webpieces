package org.webpieces.plugins.hsqldb;

import java.util.List;

import org.webpieces.router.api.routing.Plugin;
import org.webpieces.router.api.routing.Routes;

import com.google.common.collect.Lists;
import com.google.inject.Module;

public class H2DbPlugin implements Plugin {

	private H2DbConfig config;

	public H2DbPlugin(H2DbConfig config) {
		this.config = config;
	}
	
	@Override
	public List<Module> getGuiceModules() {
		return Lists.newArrayList(new H2DbModule(config));
	}

	@Override
	public List<Routes> getRouteModules() {
		return Lists.newArrayList(new H2DbRoutes(config));
	}

}
