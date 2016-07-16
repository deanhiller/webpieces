package org.webpieces.webserver.sync;

import java.util.List;

import org.webpieces.router.api.routing.RouteModule;
import org.webpieces.router.api.routing.WebAppMeta;

import com.google.common.collect.Lists;
import com.google.inject.Module;

public class BasicAppMeta implements WebAppMeta {
	public List<Module> getGuiceModules() {
		return Lists.newArrayList(new BasicModule());
	}
	
	public List<RouteModule> getRouteModules() {
		return Lists.newArrayList(new BasicRouteModule());
	}
}