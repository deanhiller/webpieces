package org.webpieces.plugins.json;

import org.webpieces.router.api.routing.AbstractRoutes;
import org.webpieces.router.api.routing.PortType;

public class JacksonRoutes extends AbstractRoutes {

	private String filterPattern;
	private Class<? extends JsonCatchAllFilter> filter;

	public JacksonRoutes(String filterPattern, 
			Class<? extends JsonCatchAllFilter> filter) {
		this.filterPattern = filterPattern;
		this.filter = filter;
	}
	
	@Override
	protected void configure() {
		//We could also add the TransactionFilter around internal error but usually that is not a good idea
		//as if the database goes down, you will end up with error to error to webpieces fail-safe 500 page which
		//does not look like your website
		//Also, we don't wrap NotFound but you could do that as well
		addFilter(filterPattern, filter, false, PortType.ALL_FILTER);
		
		addNotFoundFilter(filter, true, PortType.ALL_FILTER);
	}

}
