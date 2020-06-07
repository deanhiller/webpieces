package org.webpieces.webserver.api.login;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.ctx.api.HttpMethod.POST;
import static org.webpieces.router.api.routes.Port.HTTPS;

import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routebldr.ScopedRouteBuilder;
import org.webpieces.router.api.routes.FilterPortType;
import org.webpieces.router.api.routes.RouteId;
import org.webpieces.router.api.routes.Routes;

public abstract class AbstractLoginRoutes implements Routes {

	private String controller;
	private String secureRegEx;
	protected String basePath;
	private String[] secureFields;
	private int filterLevel;
	private boolean regExMatchPackage;

	@Deprecated
	public AbstractLoginRoutes(String controller, String basePath, String securePath, String ... secureFields) {
		this(controller, basePath, securePath, false, 10000, secureFields);
	}
	
	public AbstractLoginRoutes(String controller, String packageRegEx, int filterLevel, String ... secureFields) {
		this(controller, null, packageRegEx, true, filterLevel, secureFields);
	}
	
	public AbstractLoginRoutes(String controller, String basePath, String secureRegEx, boolean regExMatchPackage, int filterLevel, String ... secureFields) {
		this.controller = controller;
		this.basePath = basePath;
		this.secureRegEx = secureRegEx;
		this.regExMatchPackage = regExMatchPackage;
		this.filterLevel = filterLevel;
		this.secureFields = secureFields;
	}
	
	@Override
	public void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder bldr = fetchBuilder(domainRouteBldr);
		
		ScopedRouteBuilder scopedBldr = bldr;
		if(basePath != null)
			scopedBldr = bldr.getScopedRouteBuilder(basePath);
		scopedBldr.addRoute(HTTPS, GET , "/logout",              controller+".logout", getRenderLogoutRoute());		
		scopedBldr.addRoute(HTTPS, GET , "/login",               controller+".login", getRenderLoginRoute());
		scopedBldr.addRoute(HTTPS, POST, "/postLogin",           controller+".postLogin", getPostLoginRoute());

		addLoggedInHome(bldr, scopedBldr);
		
		
		LoginInfo initialConfig = new LoginInfo(getSessionToken(), getRenderLoginRoute(), secureFields);
		
		if(regExMatchPackage) {
			//being in a package secures all NotFound based on changing params so hackers can't probe urls of dynamic urls of /secure/{name} guessing names and such
			bldr.addPackageFilter(secureRegEx, LoginFilter.class, initialConfig, FilterPortType.HTTPS_FILTER, filterLevel);
			
		} else {
			bldr.addFilter(secureRegEx, LoginFilter.class, initialConfig, FilterPortType.HTTPS_FILTER, filterLevel);
			//redirects all queries for non-existent pages to a login (then the clients don't know which urls exist and don't exist which is good)
			//ie. you can only get not found AFTER logging in
			bldr.addNotFoundFilter(LoginFilter.class, new LoginInfo(secureRegEx, getSessionToken(), getRenderLoginRoute(), secureFields), FilterPortType.HTTPS_FILTER, filterLevel);
		}
	}

	protected abstract RouteBuilder fetchBuilder(DomainRouteBuilder domainRouteBldr);

	protected abstract void addLoggedInHome(RouteBuilder bldr, ScopedRouteBuilder scopedBldr);

	protected abstract String getSessionToken();

	protected abstract RouteId getPostLoginRoute();

	protected abstract RouteId getRenderLoginRoute();

	protected abstract RouteId getRenderLogoutRoute();

}
