package org.webpieces.router.impl.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.impl.MatchResult;
import org.webpieces.router.impl.RouteMeta;

public class L1AllRouting {

	//This is specifically for ONE or TWO domains really.  Use the main field for
	//1..N domains that generally use all the same routes.
	private final Map<String, L2DomainRoutes> specificDomainRoutes = new HashMap<>();

	//routes for all domains EXCEPT those in specificDomainRoutes
	final L2DomainRoutes main = new L2DomainRoutes(null);

	public L2DomainRoutes getMainRoutes() {
		return main;
	}
	
	public L2DomainRoutes getCreateDomainScoped(String domain) {
		L2DomainRoutes routes = specificDomainRoutes.get(domain);
		if(routes == null) {
			routes = new L2DomainRoutes(domain);
			specificDomainRoutes.put(domain, routes);
		}
		return routes;
	}

	public List<L2DomainRoutes> getAllDomains() {
		List<L2DomainRoutes> list = new ArrayList<>();
		list.add(main);
		list.addAll(specificDomainRoutes.values());
		return list;
	}
	
	public MatchResult fetchRoute(RouterRequest req, String relativePath) {
		String domain = req.domain;
		L2DomainRoutes domainRoutes = specificDomainRoutes.get(domain);
		if(domainRoutes != null)
			return domainRoutes.fetchRoute(req, relativePath);
		return main.fetchRoute(req, relativePath);
	}

	public RouteMeta getPageNotfoundRoute(String domain) {
		if(domain != null) {
			L2DomainRoutes domainRoutes = specificDomainRoutes.get(domain);
			if(domainRoutes != null)
				return domainRoutes.getPageNotFoundRoute();
		}
		return main.getPageNotFoundRoute();
	}

	public RouteMeta getInternalErrorRoute(String domain) {
		if(domain != null) {
			L2DomainRoutes domainRoutes = specificDomainRoutes.get(domain);
			if(domainRoutes != null)
				return domainRoutes.getInternalSvrErrorRoute();
		}
		return main.getInternalSvrErrorRoute();
	}	
}
