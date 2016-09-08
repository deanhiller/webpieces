package org.webpieces.webserver.dev.app;

import java.util.List;

import org.webpieces.router.api.routing.RouteModule;
import org.webpieces.router.api.routing.WebAppMeta;
import org.webpieces.webserver.EmptyModule;

import com.google.common.collect.Lists;
import com.google.inject.Module;

public class DevMeta implements WebAppMeta {
	public List<Module> getGuiceModules() {
		return Lists.newArrayList(new EmptyModule(),
				new NewModule());
	}
	
	public List<RouteModule> getRouteModules() {
		return Lists.newArrayList(new DevRouteModule());
	}
	
}