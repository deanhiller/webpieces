package org.webpieces.auth0.api;

import org.webpieces.auth0.impl.AuthFilter;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routes.FilterPortType;
import org.webpieces.router.api.routes.Port;
import org.webpieces.router.api.routes.RouteId;
import org.webpieces.router.api.routes.Routes;

public class Auth0Routes implements Routes {

	private AuthRouteIdSet routeIdSet;
	private String controller;
	private String packageRegEx;
	private int filterLevel;

	public Auth0Routes(AuthRouteIdSet routeIdSet, String controller, String packageRegEx, int filterLevel) {
		this.routeIdSet = routeIdSet;
		this.controller = controller;
		this.packageRegEx = packageRegEx;
		this.filterLevel = filterLevel;
	}
	
	@Override
	public void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder bldr = domainRouteBldr.getAllDomainsRouteBuilder();

		RouteId logoutRoute = routeIdSet.getLogoutRoute();
		RouteId loginRoute = routeIdSet.getLoginRoute();
		RouteId callbackRoute = routeIdSet.getCallbackRoute();

		bldr.addRoute(Port.HTTPS, HttpMethod.GET , "/logout",              controller+".logout", logoutRoute);
		bldr.addRoute(Port.HTTPS, HttpMethod.GET , "/login",               controller+".login", loginRoute);
		bldr.addRoute(Port.HTTPS, HttpMethod.GET, "/callback",           controller+".callback", callbackRoute);

		//being in a package filter secures all NotFound based on changing params so hackers can't probe urls of dynamic urls of /secure/{name}
		// guessing names and such as the filter is related to class locations not url patterns
		bldr.addPackageFilter(packageRegEx, AuthFilter.class, routeIdSet, FilterPortType.HTTPS_FILTER, filterLevel);
	}


}
