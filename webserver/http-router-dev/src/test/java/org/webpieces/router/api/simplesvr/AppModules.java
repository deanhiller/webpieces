package org.webpieces.router.api.simplesvr;

import java.util.List;
import java.util.Map;

import org.webpieces.router.api.routing.Plugin;
import org.webpieces.router.api.routing.RouteModule;
import org.webpieces.router.api.routing.WebAppMeta;

import com.google.common.collect.Lists;
import com.google.inject.Module;

public class AppModules implements WebAppMeta {

	@Override
	public void initialize(Map<String, String> props) {
	}
	
	public List<Module> getGuiceModules() {
		return Lists.newArrayList(new MtgModule());
	}
	
	public List<RouteModule> getRouteModules() {
		return Lists.newArrayList(new MtgRouteModule());
	}

	@Override
	public List<Plugin> getPlugins() {
		return Lists.newArrayList();
	}

}
