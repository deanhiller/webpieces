package org.webpieces.plugin.hibernate;

import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routes.FilterPortType;
import org.webpieces.router.api.routes.Routes;

public class HibernateRoutes implements Routes {

	private HibernateConfiguration config;

	public HibernateRoutes(HibernateConfiguration config) {
		this.config = config;
	}

	@Override
	public void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder bldr = domainRouteBldr.getAllDomainsRouteBuilder();
		//We could also add the TransactionFilter around internal error but usually that is not a good idea
		//as if the database goes down, you will end up with error to error to webpieces fail-safe 500 page which
		//does not look like your website
		//Also, we don't wrap NotFound but you could do that as well
		String filterPath = config.getFilterRegExPath(); //every path with use the filter
		int filterApplyLevel = config.getFilterApplyLevel();

		TxConfig txConfig = new TxConfig(config.isTransactionOnByDefault());
		if(config.isApplyRegExPackage()) {
			bldr.addPackageFilter(filterPath, TransactionFilter.class, txConfig, FilterPortType.ALL_FILTER, filterApplyLevel);
		} else {
			bldr.addFilter(filterPath, TransactionFilter.class, txConfig, FilterPortType.ALL_FILTER, filterApplyLevel);
		}
	}

}
