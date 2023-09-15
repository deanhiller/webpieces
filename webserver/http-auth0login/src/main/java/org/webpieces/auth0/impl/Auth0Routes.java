package org.webpieces.auth0.impl;

import org.webpieces.auth0.api.Auth0Config;
import org.webpieces.auth0.api.Auth0RouteId;
import org.webpieces.auth0.impl.AuthFilter;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routes.FilterPortType;
import org.webpieces.router.api.routes.Port;
import org.webpieces.router.api.routes.RouteId;
import org.webpieces.router.api.routes.Routes;

public class Auth0Routes implements Routes {

	private Auth0Config auth0Config;

	public Auth0Routes(Auth0Config auth0Config) {
		this.auth0Config = auth0Config;
	}
	
	@Override
	public void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder bldr = domainRouteBldr.getAllDomainsRouteBuilder();

		String packageRegEx = auth0Config.getPackageRegEx();
		int filterLevel = auth0Config.getFilterLevel();

		bldr.addRoute(Port.HTTPS, HttpMethod.GET , "/logout","Auth0Controller.logout", Auth0RouteId.LOGOUT);
		bldr.addRoute(Port.HTTPS, HttpMethod.GET , "/login","Auth0Controller.login", Auth0RouteId.LOGIN);
		bldr.addRoute(Port.HTTPS, HttpMethod.GET, "/callback","Auth0Controller.callback", Auth0RouteId.CALLBACK);

		//being in a package filter secures all NotFound based on changing params so hackers can't probe urls of dynamic urls of /secure/{name}
		// guessing names and such as the filter is related to class locations not url patterns
		bldr.addPackageFilter(packageRegEx, AuthFilter.class, auth0Config, FilterPortType.HTTPS_FILTER, filterLevel);
	}


}
