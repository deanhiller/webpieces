package org.webpieces.router.api.routes;

import java.util.Arrays;
import java.util.List;

import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.impl.model.RouteModuleInfo;
import org.webpieces.router.impl.routebldr.CurrentPackage;

public class ScopedDomainRoutes implements Routes {

	private String domain;
	private List<BasicRoutes> modules;

	public ScopedDomainRoutes(String domain, BasicRoutes ... modules) {
		if(domain == null || domain.length() == 0)
			throw new IllegalArgumentException("domain cannot be null and must be larger than size 0");
		this.domain = domain;
		this.modules = Arrays.asList(modules);
	}

	@Override
	public void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder scopedRouter = domainRouteBldr.getDomainScopedBuilder(domain).getBldrForAllOtherContentTypes();
		
		for(BasicRoutes module : modules) {
			CurrentPackage.set(new RouteModuleInfo(module.getClass()));
			module.configure(scopedRouter);
			CurrentPackage.set(null);
		}
	}
	
	@Override
	public String toString() {
		return "ScopedDomainModule [domain=" + domain + ", modules=" + modules + "]";
	}
}
