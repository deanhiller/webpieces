package org.webpieces.plugins.json;

import java.util.regex.Pattern;

import org.webpieces.router.api.routing.PortType;
import org.webpieces.router.api.routing.Routes;
import org.webpieces.router.impl.model.bldr.DomainRouteBuilder;
import org.webpieces.router.impl.model.bldr.RouteBuilder;

public class JacksonRoutes implements Routes {

	private String filterPattern;
	private Class<? extends JacksonCatchAllFilter> filter;

	public JacksonRoutes(JacksonConfig config) {
		this.filterPattern = config.getFilterPattern();
		this.filter = config.getFilterClazz();
	}
	
	@Override
	public void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder bldr = domainRouteBldr.getAllDomainsRouteBuilder();
		Pattern pattern = Pattern.compile(filterPattern);
		
		bldr.addFilter(filterPattern, filter, new JsonConfig(pattern, false), PortType.ALL_FILTER);		
		bldr.addNotFoundFilter(filter, new JsonConfig(pattern, true), PortType.ALL_FILTER);
	}

}
