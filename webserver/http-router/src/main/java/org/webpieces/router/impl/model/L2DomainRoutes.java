package org.webpieces.router.impl.model;

import java.util.List;

import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.impl.RouteMeta;

public class L2DomainRoutes {

	private final L3PrefixedRouting domainRoutes = new L3PrefixedRouting();

	private RouteMeta pageNotFoundRoute;
	private RouteMeta internalSvrErrorRoute;

	private String domain;
	
	public L2DomainRoutes(String domain) {
		this.domain = domain;
	}

	public L3PrefixedRouting getRoutesForDomain() {
		return domainRoutes;
	}
	
	public L3PrefixedRouting getScopedRouter(String path) {
		return domainRoutes.getScopedRouter(path);
	}

	public void setPageNotFoundRoute(RouteMeta meta) {
		//to help them find weird bugs, throw if they set this twice...
		if(pageNotFoundRoute != null)
			throw new IllegalStateException("Page Not found for domain="+domain+" was already set.  cannot set again");
		this.pageNotFoundRoute = meta;
	}
	public void setInternalSvrErrorRoute(RouteMeta meta) {
		if(internalSvrErrorRoute != null)
			throw new IllegalStateException("InternalSvrError Route for domain="+domain+" was already set.  cannot set again");
		this.internalSvrErrorRoute = meta;
	}
	public RouteMeta getPageNotFoundRoute() {
		return pageNotFoundRoute;
	}
	public RouteMeta getInternalSvrErrorRoute() {
		return internalSvrErrorRoute;
	}

	public String getDomain() {
		return domain;
	}

	public MatchResult fetchRoute(List<RouteMeta> staticRoutes, RouterRequest req, String relativePath) {
		MatchResult result = domainRoutes.fetchRoute(req, relativePath);
		if(!result.isFound()) {
			result = domainRoutes.findRouteMatch(staticRoutes, req, relativePath);
		}
		
		if(!result.isFound()) //if still not found in static routes, return pageNotFoundRoute
			return new MatchResult(pageNotFoundRoute);
		
		return result;
	}
	
}
