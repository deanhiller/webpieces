package org.webpieces.router.api.simplesvr;

import java.util.List;
import java.util.Map;

import org.webpieces.router.api.plugins.Plugin;
import org.webpieces.router.api.routes.Routes;
import org.webpieces.router.api.routes.WebAppMeta;

import com.google.common.collect.Lists;
import com.google.inject.Module;

public class AppModules implements WebAppMeta {

	@Override
	public void initialize(Map<String, String> props) {
	}
	
	public List<Module> getGuiceModules() {
		return Lists.newArrayList(new MtgModule());
	}
	
	public List<Routes> getRouteModules() {
		return Lists.newArrayList(new MtgRoutes());
	}

	@Override
	public List<Plugin> getPlugins() {
		return Lists.newArrayList();
	}

}
