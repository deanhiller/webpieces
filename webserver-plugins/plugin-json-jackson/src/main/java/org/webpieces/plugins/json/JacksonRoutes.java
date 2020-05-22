package org.webpieces.plugins.json;

import java.util.regex.Pattern;

import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routes.FilterPortType;
import org.webpieces.router.api.routes.Routes;

public class JacksonRoutes implements Routes {

	private String filterPattern;
	private Class<? extends JacksonCatchAllFilter> filter;
	private JacksonConfig config;

	public JacksonRoutes(JacksonConfig config) {
		this.config = config;
		this.filterPattern = config.getFilterPattern();
		this.filter = config.getFilterClazz();
	}
	
	@Override
	public void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder bldr = domainRouteBldr.getAllDomainsRouteBuilder();
		Pattern pattern = Pattern.compile(filterPattern);
		
		int filterLevel = 1000000;
		if(config.getFilterApplyLevel() != null)
			filterLevel = config.getFilterApplyLevel();
		
		bldr.addFilter(filterPattern, filter, new JsonConfig(pattern, false), FilterPortType.ALL_FILTER, filterLevel);		
		bldr.addNotFoundFilter(filter, new JsonConfig(pattern, true), FilterPortType.ALL_FILTER, filterLevel);
	}

}
