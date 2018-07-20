package org.webpieces.router.api.error.dev;

import java.util.List;
import java.util.Map;

import org.webpieces.router.api.SimpleStorage;
import org.webpieces.router.api.routing.Plugin;
import org.webpieces.router.api.routing.Routes;
import org.webpieces.router.api.routing.WebAppMeta;
import org.webpieces.router.api.simplesvr.EmptyStorage;

import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.google.inject.Module;

public class NoMethodRouterModules implements WebAppMeta {

	@Override
	public void initialize(Map<String, String> props) {
	}
	
	public List<Module> getGuiceModules() {
		return Lists.newArrayList(new Module() {
			@Override
			public void configure(Binder binder) {
				binder.bind(SimpleStorage.class).toInstance(new EmptyStorage());
			}});
	}
	
	public List<Routes> getRouteModules() {
		return Lists.newArrayList(new NoMethodRoutes());
	}

	@Override
	public List<Plugin> getPlugins() {
		return null;
	}
}
