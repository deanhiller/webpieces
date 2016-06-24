package org.webpieces.router.api.routing;

import java.util.List;

import com.google.inject.Module;

public interface WebAppMeta {

	public List<Module> getGuiceModules();
	
	public List<RouteModule> getRouteModules();
	
}
