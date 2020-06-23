package org.webpieces.plugin.dto;

import java.util.List;

import org.webpieces.router.api.plugins.Plugin;
import org.webpieces.router.api.routes.Routes;
import org.webpieces.util.cmdline2.Arguments;

import com.google.common.collect.Lists;
import com.google.inject.Module;

public class DtoPlugin implements Plugin {

	private DtoConfiguration config;

	public DtoPlugin(DtoConfiguration config, Arguments cmdLineArgs) {
		this.config = config;
	}
	
	@Override
	public List<Module> getGuiceModules() {
		return Lists.newArrayList(new DtoModule(config));
	}

	@Override
	public List<Routes> getRouteModules() {
		return null;
	}

}
