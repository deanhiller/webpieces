package org.webpieces.plugins.json;

import java.util.List;

import org.webpieces.router.api.routing.Plugin;
import org.webpieces.router.api.routing.Routes;

import com.google.common.collect.Lists;
import com.google.inject.Module;

public class JacksonPlugin implements Plugin {

	private JacksonConfig config;
	
	public JacksonPlugin(JacksonConfig config) {
		super();
		this.config = config;
	}
	
	@Override
	public List<Module> getGuiceModules() {
		return Lists.newArrayList(new JacksonModule());
	}

	@Override
	public List<Routes> getRouteModules() {
		return Lists.newArrayList(new JacksonRoutes(config));
	}

}
