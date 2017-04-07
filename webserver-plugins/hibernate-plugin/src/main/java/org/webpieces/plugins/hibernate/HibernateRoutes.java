package org.webpieces.plugins.hibernate;

import org.webpieces.router.api.routing.AbstractRoutes;
import org.webpieces.router.api.routing.PortType;

public class HibernateRoutes extends AbstractRoutes {

	@Override
	protected void configure() {
		//We could also add the TransactionFilter around internal error but usually that is not a good idea
		//as if the database goes down, you will end up with error to error to webpieces fail-safe 500 page which
		//does not look like your website
		//Also, we don't wrap NotFound but you could do that as well
		addFilter(".*", TransactionFilter.class, null, PortType.ALL_FILTER);
	}

}
