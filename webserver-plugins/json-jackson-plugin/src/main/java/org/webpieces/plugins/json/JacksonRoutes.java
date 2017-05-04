package org.webpieces.plugins.json;

import java.util.regex.Pattern;

import org.webpieces.router.api.routing.AbstractRoutes;
import org.webpieces.router.api.routing.PortType;

public class JacksonRoutes extends AbstractRoutes {

	private String filterPattern;
	private Class<? extends JacksonCatchAllFilter> filter;

	public JacksonRoutes(String filterPattern, 
			Class<? extends JacksonCatchAllFilter> filter) {
		this.filterPattern = filterPattern;
		this.filter = filter;
	}
	
	@Override
	protected void configure() {
		Pattern pattern = Pattern.compile(filterPattern);
		
		addFilter(filterPattern, filter, new JsonConfig(pattern, false), PortType.ALL_FILTER);		
		addNotFoundFilter(filter, new JsonConfig(pattern, true), PortType.ALL_FILTER);
	}

}
