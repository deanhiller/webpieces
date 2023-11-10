package org.webpieces.googleauth.impl;

import org.webpieces.googleauth.api.GoogleAuthConfig;
import org.webpieces.googleauth.api.GoogleAuthRouteId;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routes.FilterPortType;
import org.webpieces.router.api.routes.Port;
import org.webpieces.router.api.routes.Routes;

public class AuthRoutes implements Routes {

	private GoogleAuthConfig auth0Config;

	public AuthRoutes(GoogleAuthConfig auth0Config) {
		this.auth0Config = auth0Config;
	}
	
	@Override
	public void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder bldr = domainRouteBldr.getAllDomainsRouteBuilder();

		String packageRegEx = auth0Config.getPackageRegEx();
		int filterLevel = auth0Config.getFilterLevel();

		bldr.addRoute(Port.HTTPS, HttpMethod.GET , "/logout","AuthController.logout", GoogleAuthRouteId.LOGOUT);
		bldr.addRoute(Port.HTTPS, HttpMethod.GET , "/login","AuthController.login", GoogleAuthRouteId.LOGIN);
		bldr.addRoute(Port.HTTPS, HttpMethod.GET, "/callback","AuthController.callback", GoogleAuthRouteId.CALLBACK);

		//being in a package filter secures all NotFound based on changing params so hackers can't probe urls of dynamic urls of /secure/{name}
		// guessing names and such as the filter is related to class locations not url patterns
		bldr.addPackageFilter(packageRegEx, AuthFilter.class, auth0Config, FilterPortType.HTTPS_FILTER, filterLevel);
	}


}
