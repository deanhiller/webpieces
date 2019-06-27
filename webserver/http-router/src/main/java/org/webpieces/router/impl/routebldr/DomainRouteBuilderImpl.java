package org.webpieces.router.impl.routebldr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.impl.ResettingLogic;
import org.webpieces.router.impl.model.RouteBuilderLogic;
import org.webpieces.router.impl.routers.BDomainRouter;
import org.webpieces.router.impl.routers.CRouter;
import org.webpieces.router.impl.routers.EStaticRouter;

public class DomainRouteBuilderImpl implements DomainRouteBuilder {

	private final RouteBuilderLogic holder;

	private final RouteBuilderImpl leftOverDomainsBuilder;
	private final RouteBuilderImpl backendRouteBuilder;
	private final Map<String, RouteBuilderImpl> domainToRouteBuilder = new HashMap<>();

	private ResettingLogic resettingLogic;
	
	public DomainRouteBuilderImpl(RouteBuilderLogic holder, ResettingLogic resettingLogic, boolean isExposeBackendOnInternalPort) {
		this.holder = holder;
		this.resettingLogic = resettingLogic;
		this.leftOverDomainsBuilder = new RouteBuilderImpl("<any>", holder, resettingLogic);
		if(isExposeBackendOnInternalPort)
			this.backendRouteBuilder = new RouteBuilderImpl("<backend>", holder, resettingLogic);
		else {
			this.backendRouteBuilder = this.leftOverDomainsBuilder;
		}
	}

	@Override
	public RouteBuilder getAllDomainsRouteBuilder() {
		return leftOverDomainsBuilder;
	}

	@Override
	public RouteBuilder getBackendRouteBuilder() {
		return backendRouteBuilder;
	}
	
	@Override
	public RouteBuilder getDomainScopedRouteBuilder(String domain) {
		RouteBuilderImpl builder = domainToRouteBuilder.get(domain);
		if(builder != null)
			return builder;
		
		builder = new RouteBuilderImpl(domain, holder, resettingLogic);
		domainToRouteBuilder.put(domain, builder);
		return builder;
	}

	public BDomainRouter build() {
		CRouter router = leftOverDomainsBuilder.buildRouter();
		CRouter backendRouter = backendRouteBuilder.buildRouter();
		
		Map<String, CRouter> domainToRouter = new HashMap<>();
		for(Entry<String, RouteBuilderImpl> entry : domainToRouteBuilder.entrySet()) {
			CRouter router2 = entry.getValue().buildRouter();
			domainToRouter.put(entry.getKey(), router2);
		}
		
		return new BDomainRouter(router, backendRouter, domainToRouter);
	}

	public List<EStaticRouter> getStaticRoutes() {
		List<EStaticRouter> staticRouters = new ArrayList<>();
		for(Entry<String, RouteBuilderImpl> entry : domainToRouteBuilder.entrySet()) {
			staticRouters.addAll(entry.getValue().getStaticRoutes());
		}		
		
		staticRouters.addAll(leftOverDomainsBuilder.getStaticRoutes());
		return staticRouters;
	}

}
