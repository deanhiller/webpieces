package org.webpieces.router.impl.model.bldr.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.webpieces.router.impl.StaticRoute;
import org.webpieces.router.impl.model.LogicHolder;
import org.webpieces.router.impl.model.bldr.DomainRouteBuilder;
import org.webpieces.router.impl.model.bldr.RouteBuilder;
import org.webpieces.router.impl.model.bldr.data.DomainRouter;
import org.webpieces.router.impl.model.bldr.data.Router;

public class DomainRouteBuilderImpl implements DomainRouteBuilder {
	
	private final RouteBuilderImpl leftOverDomainsBuilder;
	private final Map<String, RouteBuilderImpl> domainToRouteBuilder = new HashMap<>();
	private final LogicHolder holder;
	private final List<StaticRoute> allStaticRoutes;
	
	public DomainRouteBuilderImpl(LogicHolder holder) {
		this.holder = holder;
		this.allStaticRoutes = new ArrayList<>();
		this.leftOverDomainsBuilder = new RouteBuilderImpl("<any>", allStaticRoutes, holder);
	}

	@Override
	public RouteBuilder getAllDomainsRouteBuilder() {
		return leftOverDomainsBuilder;
	}

	@Override
	public RouteBuilder getDomainScopedRouteBuilder(String domain) {
		RouteBuilderImpl builder = domainToRouteBuilder.get(domain);
		if(builder != null)
			return builder;
		
		builder = new RouteBuilderImpl(domain, allStaticRoutes, holder);
		domainToRouteBuilder.put(domain, builder);
		return builder;
	}

	public DomainRouter build() {
		Router router = leftOverDomainsBuilder.buildRouter();
		
		Map<String, Router> domainToRouter = new HashMap<>();
		for(Entry<String, RouteBuilderImpl> entry : domainToRouteBuilder.entrySet()) {
			Router router2 = entry.getValue().buildRouter();
			domainToRouter.put(entry.getKey(), router2);
		}
		
		return new DomainRouter(router, domainToRouter);
	}

	public List<StaticRoute> getStaticRoutes() {
		return allStaticRoutes;
	}
}
