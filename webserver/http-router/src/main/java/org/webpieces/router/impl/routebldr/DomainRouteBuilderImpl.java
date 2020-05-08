package org.webpieces.router.impl.routebldr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.AllContentTypesBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.impl.ResettingLogic;
import org.webpieces.router.impl.model.RouteBuilderLogic;
import org.webpieces.router.impl.routers.BRouter;
import org.webpieces.router.impl.routers.CRouter;
import org.webpieces.router.impl.routers.FStaticRouter;
import org.webpieces.util.futures.FutureHelper;

public class DomainRouteBuilderImpl implements DomainRouteBuilder {

	private final RouteBuilderLogic holder;

	private final AllContentTypesBuilderImpl leftOverDomainsBuilder2;
	private final AllContentTypesBuilderImpl backendRouteBuilder2;
	private final Map<String, AllContentTypesBuilderImpl> domainToRouteBuilder2 = new HashMap<>();
	
//	private final RouteBuilderImpl leftOverDomainsBuilder;
//	private final RouteBuilderImpl backendRouteBuilder;
//	private final Map<String, RouteBuilderImpl> domainToRouteBuilder = new HashMap<>();

	private ResettingLogic resettingLogic;

	private FutureHelper futureUtil;

	public DomainRouteBuilderImpl(FutureHelper futureUtil, RouteBuilderLogic holder, ResettingLogic resettingLogic, boolean useUniqueBackendRouter) {
		this.futureUtil = futureUtil;
		this.holder = holder;
		this.resettingLogic = resettingLogic;
		this.leftOverDomainsBuilder2 = new AllContentTypesBuilderImpl("<any>", holder, resettingLogic, futureUtil);
		if(useUniqueBackendRouter)
			this.backendRouteBuilder2 = new AllContentTypesBuilderImpl("<backend>", holder, resettingLogic, futureUtil);
		else {
			this.backendRouteBuilder2 = this.leftOverDomainsBuilder2;
		}
	}	

	@Override
	public RouteBuilder getAllDomainsRouteBuilder() {
		return leftOverDomainsBuilder2.getBldrForAllOtherContentTypes();
	}

	public BRouter build() {
		CRouter router = leftOverDomainsBuilder2.buildRouter();
		CRouter backendRouter = backendRouteBuilder2.buildRouter();
		
		Map<String, CRouter> domainToRouter = new HashMap<>();
		for(Entry<String, AllContentTypesBuilderImpl> entry : domainToRouteBuilder2.entrySet()) {
			CRouter router2 = entry.getValue().buildRouter();
			domainToRouter.put(entry.getKey(), router2);
		}
		
		return new BRouter(router, backendRouter, domainToRouter);
	}

	public List<FStaticRouter> getStaticRoutes() {
		List<FStaticRouter> staticRouters = new ArrayList<>();
		for(Entry<String, AllContentTypesBuilderImpl> entry : domainToRouteBuilder2.entrySet()) {
			staticRouters.addAll(entry.getValue().getBldrForAllOtherContentTypes().getStaticRoutes());
		}		
		
		staticRouters.addAll(leftOverDomainsBuilder2.getBldrForAllOtherContentTypes().getStaticRoutes());
		return staticRouters;
	}

	@Override
	public AllContentTypesBuilder getBuilderForAllOtherDomains() {
		return leftOverDomainsBuilder2;
	}

	@Override
	public AllContentTypesBuilder getDomainScopedBuilder(String domainRegEx) {
		AllContentTypesBuilderImpl builder = domainToRouteBuilder2.get(domainRegEx);
		if(builder != null)
			return builder;
		
		builder = new AllContentTypesBuilderImpl(domainRegEx, holder, resettingLogic, futureUtil);
		domainToRouteBuilder2.put(domainRegEx, builder);
		return builder;
	}

	//NOT NEEDED YET so don't expose to keep things simple
	@Override
	public AllContentTypesBuilder getBackendBuilder() {
		return backendRouteBuilder2;
	}
}
