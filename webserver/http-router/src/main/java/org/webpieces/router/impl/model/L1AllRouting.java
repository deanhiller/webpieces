package org.webpieces.router.impl.model;

import java.util.HashMap;
import java.util.Map;

public class L1AllRouting {

	//This is specifically for ONE or TWO domains really.  Use the main field for
	//1..N domains that generally use all the same routes.
	private final Map<String, L2DomainRoutes> specificDomainRoutes = new HashMap<>();

	//routes for all domains EXCEPT those in specificDomainRoutes
	final L2DomainRoutes main = new L2DomainRoutes(".*");

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

	
	
}
