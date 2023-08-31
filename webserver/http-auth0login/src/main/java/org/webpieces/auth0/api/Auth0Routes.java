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

	private Auth0Config auth0Config;

	public Auth0Routes(Auth0Config auth0Config) {
		this.auth0Config = auth0Config;
	}
	
	@Override
	public void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder bldr = domainRouteBldr.getAllDomainsRouteBuilder();

		RouteId logoutRoute = auth0Config.getLogoutRoute();
		RouteId loginRoute = auth0Config.getLoginRoute();
		RouteId callbackRoute = auth0Config.getCallbackRoute();

		String controller = auth0Config.getController();
		String packageRegEx = auth0Config.getPackageRegEx();
		int filterLevel = auth0Config.getFilterLevel();

		bldr.addRoute(Port.HTTPS, HttpMethod.GET , "/logout",              controller+".logout", logoutRoute);
		bldr.addRoute(Port.HTTPS, HttpMethod.GET , "/login",               controller+".login", loginRoute);
		bldr.addRoute(Port.HTTPS, HttpMethod.GET, "/callback",           controller+".callback", callbackRoute);

		//being in a package filter secures all NotFound based on changing params so hackers can't probe urls of dynamic urls of /secure/{name}
		// guessing names and such as the filter is related to class locations not url patterns
		bldr.addPackageFilter(packageRegEx, AuthFilter.class, auth0Config, FilterPortType.HTTPS_FILTER, filterLevel);
	}


}
