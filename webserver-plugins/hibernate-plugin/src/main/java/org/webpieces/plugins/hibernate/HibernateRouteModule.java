package org.webpieces.plugins.hibernate;

import org.webpieces.router.api.routing.AbstractRouteModule;
import org.webpieces.router.api.routing.PortType;

public class HibernateRouteModule extends AbstractRouteModule {

	@Override
	protected void configure(String currentPackage) {
		addFilter(".*", TransactionFilter.class, null, PortType.ALL_FILTER);
	}

}
