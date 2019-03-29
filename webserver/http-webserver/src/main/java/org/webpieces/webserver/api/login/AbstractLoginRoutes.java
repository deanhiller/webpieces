package org.webpieces.webserver.api.login;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.ctx.api.HttpMethod.POST;
import static org.webpieces.router.api.routing.Port.HTTPS;

import org.webpieces.router.api.routing.PortType;
import org.webpieces.router.api.routing.RouteId;
import org.webpieces.router.api.routing.Routes;
import org.webpieces.router.impl.model.bldr.DomainRouteBuilder;
import org.webpieces.router.impl.model.bldr.RouteBuilder;
import org.webpieces.router.impl.model.bldr.ScopedRouteBuilder;

public abstract class AbstractLoginRoutes implements Routes {

	private String controller;
	private String securePath;
	protected String basePath;
	private String[] secureFields;

	public AbstractLoginRoutes(String controller, String basePath, String securePath, String ... secureFields) {
		this.controller = controller;
		this.basePath = basePath;
		this.securePath = securePath;
		this.secureFields = secureFields;
	}
	
	@Override
	public void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder bldr = domainRouteBldr.getAllDomainsRouteBuilder();
		
		ScopedRouteBuilder scopedBldr = bldr;
		if(basePath != null)
			scopedBldr = bldr.getScopedRouteBuilder(basePath);
		scopedBldr.addRoute(HTTPS, GET , "/logout",              controller+".logout", getRenderLogoutRoute());		
		scopedBldr.addRoute(HTTPS, GET , "/login",               controller+".login", getRenderLoginRoute());
		scopedBldr.addRoute(HTTPS, POST, "/postLogin",           controller+".postLogin", getPostLoginRoute());

		addLoggedInHome(bldr, scopedBldr);
		
		bldr.addFilter(securePath, LoginFilter.class, new LoginInfo(getSessionToken(), getRenderLoginRoute(), secureFields), PortType.HTTPS_FILTER);
		//redirects all queries for non-existent pages to a login (then the clients don't know which urls exist and don't exist which is good)
		//ie. you can only get not found AFTER logging in
		bldr.addNotFoundFilter(LoginFilter.class, new LoginInfo(securePath, getSessionToken(), getRenderLoginRoute(), secureFields), PortType.HTTPS_FILTER);
	}

	protected abstract void addLoggedInHome(RouteBuilder bldr, ScopedRouteBuilder scopedBldr);

	protected abstract String getSessionToken();

	protected abstract RouteId getPostLoginRoute();

	protected abstract RouteId getRenderLoginRoute();

	protected abstract RouteId getRenderLogoutRoute();

}
