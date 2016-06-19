package org.webpieces.webserver.api;

import java.util.List;

import org.webpieces.router.api.routing.RouteModule;
import org.webpieces.router.api.routing.WebAppMetaInfo;

import com.google.common.collect.Lists;
import com.google.inject.Module;

public class MyTestModule implements WebAppMetaInfo {

	public List<Module> getGuiceModules() {
		return Lists.newArrayList(new MyTestAppModule());
	}
	
	public List<RouteModule> getRouterModules() {
		return Lists.newArrayList(new MyTestRouteModule());
	}

}
