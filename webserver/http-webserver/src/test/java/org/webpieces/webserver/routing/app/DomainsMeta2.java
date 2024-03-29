package org.webpieces.webserver.routing.app;

import java.util.List;

import org.webpieces.plugin.json.JacksonConfig;
import org.webpieces.plugin.json.JacksonPlugin;
import org.webpieces.router.api.plugins.Plugin;
import org.webpieces.router.api.routes.Routes;
import org.webpieces.router.api.routes.ScopedDomainRoutes;
import org.webpieces.router.api.routes.WebAppConfig;
import org.webpieces.router.api.routes.WebAppMeta;
import org.webpieces.webserver.EmptyModule;

import com.google.common.collect.Lists;
import com.google.inject.Module;

public class DomainsMeta2 implements WebAppMeta {
	@Override
	public void initialize(WebAppConfig pluginConfig) {
	}
	@Override
    public List<Module> getGuiceModules() {
		return Lists.newArrayList(new EmptyModule());
	}
	
	@Override
    public List<Routes> getRouteModules() {
		Routes domainModule = new ScopedDomainRoutes("mydomain.com", new Domain1Routes());

		return Lists.newArrayList(
				domainModule,
				new Domain2Routes(),
				new CorsForTwoDomains(),
				new CorsForAllDomains(),
				new NotCorsRoutes(),
				new CorsTestCookie()
		);
	}
	
	@Override
	public List<Plugin> getPlugins() {
		return List.of(new JacksonPlugin(new JacksonConfig("/json/.*")));
	}
}