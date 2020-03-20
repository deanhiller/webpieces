package org.webpieces.webserver.domains.app;

import com.google.common.collect.Lists;
import com.google.inject.Module;
import org.webpieces.router.api.plugins.Plugin;
import org.webpieces.router.api.routes.Routes;
import org.webpieces.router.api.routes.ScopedDomainRoutes;
import org.webpieces.router.api.routes.WebAppConfig;
import org.webpieces.router.api.routes.WebAppMeta;
import org.webpieces.webserver.EmptyModule;

import java.util.List;

public class DomainsMeta implements WebAppMeta {
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

		return Lists.newArrayList(domainModule, new Domain2Routes());
	}
	
	@Override
	public List<Plugin> getPlugins() {
		return null;
	}
}