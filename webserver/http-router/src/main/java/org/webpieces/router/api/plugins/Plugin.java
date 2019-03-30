package org.webpieces.router.api.plugins;

import java.util.List;

import org.webpieces.router.api.routes.Routes;

import com.google.inject.Module;

public interface Plugin {
	public List<Module> getGuiceModules();
	
	public List<Routes> getRouteModules();
}
