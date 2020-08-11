package org.webpieces.plugin.json;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routes.FilterPortType;
import org.webpieces.router.api.routes.Routes;

public class JacksonRoutes implements Routes {

	private static final Logger log = LoggerFactory.getLogger(JacksonRoutes.class);
	
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
		
		if(filter != null) {
			log.info("Installing Json Catch All filter="+filter.getName());
			bldr.addFilter(filterPattern, filter, new JsonConfig(pattern), FilterPortType.ALL_FILTER, filterLevel);
		} else {
			log.info("No catch all json filter installed.  PLEASE install your own somewhere so clean errors are sent back");
		}
		bldr.addNotFoundFilter(notFoundFilter, new JsonConfig(pattern), FilterPortType.ALL_FILTER, notFoundLevel);
	}

}
