package org.webpieces.plugins.json;

import java.util.regex.Pattern;

import org.webpieces.router.api.routing.AbstractRoutes;
import org.webpieces.router.api.routing.PortType;

public class JacksonRoutes extends AbstractRoutes {

	private String filterPattern;
	private Class<? extends JacksonCatchAllFilter> filter;

	public JacksonRoutes(JacksonConfig config) {
		this.filterPattern = config.getFilterPattern();
		this.filter = config.getFilterClazz();
	}
	
	@Override
	protected void configure() {
		Pattern pattern = Pattern.compile(filterPattern);
		
		addFilter(filterPattern, filter, new JsonConfig(pattern, false), PortType.ALL_FILTER);		
		addNotFoundFilter(filter, new JsonConfig(pattern, true), PortType.ALL_FILTER);
	}

}
