package org.webpieces.plugin.json;

import java.util.regex.Pattern;

import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routes.FilterPortType;
import org.webpieces.router.api.routes.Routes;

public class JacksonRoutes implements Routes {

	private String filterPattern;
	private Class<? extends JacksonCatchAllFilter> filter;
	private Class<? extends JacksonNotFoundFilter> notFoundFilter;
	private JacksonConfig config;

	public JacksonRoutes(JacksonConfig config) {
		this.config = config;
		this.filterPattern = config.getFilterPattern();
		this.filter = config.getFilterClazz();
		this.notFoundFilter = config.getNotFoundFilterClazz();
	}
	
	@Override
	public void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder bldr = domainRouteBldr.getAllDomainsRouteBuilder();
		Pattern pattern = Pattern.compile(filterPattern);
		
		int filterLevel = config.getFilterApplyLevel();
		int notFoundLevel = config.getNotFoudFilterLevel();
		
		bldr.addFilter(filterPattern, filter, new JsonConfig(pattern), FilterPortType.ALL_FILTER, filterLevel);		
		bldr.addNotFoundFilter(notFoundFilter, new JsonConfig(pattern), FilterPortType.ALL_FILTER, notFoundLevel);
	}

}
