package org.webpieces.router.api.routing;

import org.webpieces.router.impl.model.AbstractRouteBuilder;
import org.webpieces.router.impl.model.RouteModuleInfo;

public class ScopedDomainModule extends AbstractRouteModule {
	
	private String domain;
	private RouteModule[] modules;

	public ScopedDomainModule(String domain, RouteModule ... modules) {
		if(domain == null || domain.length() == 0)
			throw new IllegalArgumentException("domain cannot be null and must be larger than size 0");
		this.domain = domain;
		this.modules = modules;
	}

	@Override
	protected void configure() {
		this.router = router.getDomainScopedRouter(domain);
		
		for(RouteModule module : modules) {
			AbstractRouteBuilder.currentPackage.set(new RouteModuleInfo(module));
			module.configure(router);
			AbstractRouteBuilder.currentPackage.set(null);
		}
	}

}
