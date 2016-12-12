package org.webpieces.plugins.hsqldb;

import java.util.List;

import org.webpieces.router.api.routing.RouteModule;
import org.webpieces.router.api.routing.WebAppMeta;

import com.google.common.collect.Lists;
import com.google.inject.Module;

public class H2DbPlugin implements WebAppMeta {

	@Override
	public List<Module> getGuiceModules() {
		return Lists.newArrayList(new H2DbModule());
	}

	@Override
	public List<RouteModule> getRouteModules() {
		return Lists.newArrayList(new H2DbRouteModule());
	}

}
