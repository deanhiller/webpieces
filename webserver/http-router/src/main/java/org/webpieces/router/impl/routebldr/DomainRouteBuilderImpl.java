package org.webpieces.router.impl.routebldr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.impl.ResettingLogic;
import org.webpieces.router.impl.StaticRoute;
import org.webpieces.router.impl.model.RouteBuilderLogic;
import org.webpieces.router.impl.routers.DomainRouter;
import org.webpieces.router.impl.routers.Router;

public class DomainRouteBuilderImpl implements DomainRouteBuilder {

	private final RouteBuilderLogic holder;

	private final RouteBuilderImpl leftOverDomainsBuilder;
	private final Map<String, RouteBuilderImpl> domainToRouteBuilder = new HashMap<>();
	private final List<StaticRoute> allStaticRoutes;

	private ResettingLogic resettingLogic;
	
	public DomainRouteBuilderImpl(RouteBuilderLogic holder, ResettingLogic resettingLogic) {
		this.holder = holder;
		this.resettingLogic = resettingLogic;
		this.allStaticRoutes = new ArrayList<>();
		this.leftOverDomainsBuilder = new RouteBuilderImpl("<any>", allStaticRoutes, holder, resettingLogic);
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
		
		builder = new RouteBuilderImpl(domain, allStaticRoutes, holder, resettingLogic);
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
