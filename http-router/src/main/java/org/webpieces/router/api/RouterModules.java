package org.webpieces.router.api;

import java.util.List;

import com.google.inject.Module;

public interface RouterModules {

	public List<Module> getGuiceModules();
	
	public List<RouteModule> getRouterModules();
	
}
