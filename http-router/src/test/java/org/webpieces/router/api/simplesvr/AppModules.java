package org.webpieces.router.api.simplesvr;

import java.util.List;

import org.webpieces.router.api.RouteModule;
import org.webpieces.router.api.RouterModules;

import com.google.common.collect.Lists;
import com.google.inject.Module;

public class AppModules implements RouterModules {

	public List<Module> getGuiceModules() {
		return Lists.newArrayList(new MtgModule());
	}
	
	public List<RouteModule> getRouterModules() {
		return Lists.newArrayList(new MtgRouteModule());
	}
}
