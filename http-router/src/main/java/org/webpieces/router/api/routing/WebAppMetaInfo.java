package org.webpieces.router.api.routing;

import java.util.List;

import com.google.inject.Module;

public interface WebAppMetaInfo {

	public List<Module> getGuiceModules();
	
	public List<RouteModule> getRouterModules();
	
}
