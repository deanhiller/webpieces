package org.webpieces.plugin.grpcjson;

import java.util.List;

import org.webpieces.router.api.plugins.Plugin;
import org.webpieces.router.api.routes.Routes;

import com.google.common.collect.Lists;
import com.google.inject.Module;

public class GrpcJsonPlugin implements Plugin {

	private GrpcJsonConfig config;
	
	public GrpcJsonPlugin(GrpcJsonConfig config) {
		super();
		this.config = config;
	}
	
	@Override
	public List<Module> getGuiceModules() {
		return Lists.newArrayList(new GrpcJsonModule());
	}

	@Override
	public List<Routes> getRouteModules() {
		return Lists.newArrayList(new GrpcJsonRoutes(config));
	}

}
