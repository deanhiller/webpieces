package org.webpieces.router.api.error.dev;

import java.util.List;

import org.webpieces.router.api.routing.RouteModule;
import org.webpieces.router.api.routing.WebAppMetaInfo;

import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.google.inject.Module;

public class CommonRoutesModules implements WebAppMetaInfo {

	public List<Module> getGuiceModules() {
		return Lists.newArrayList(new Module() {
			@Override
			public void configure(Binder binder) {
			}});
	}
	
	public List<RouteModule> getRouterModules() {
		return Lists.newArrayList(new CommonRouteModule());
	}
}
