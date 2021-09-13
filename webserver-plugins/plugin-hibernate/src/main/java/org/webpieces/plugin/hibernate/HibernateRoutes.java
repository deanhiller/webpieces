package org.webpieces.plugin.hibernate;

import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routes.FilterPortType;
import org.webpieces.router.api.routes.Routes;

public class HibernateRoutes implements Routes {

	private HibernateConfiguration config;
	private boolean transactionOnByDefault;

	public HibernateRoutes(HibernateConfiguration config, boolean transactionOnByDefault) {
		this.config = config;
		this.transactionOnByDefault = transactionOnByDefault;
	}

	@Override
	public void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder bldr = domainRouteBldr.getAllDomainsRouteBuilder();
		//We could also add the TransactionFilter around internal error but usually that is not a good idea
		//as if the database goes down, you will end up with error to error to webpieces fail-safe 500 page which
		//does not look like your website
		//Also, we don't wrap NotFound but you could do that as well
		String filterPath = ".*"; //every path with use the filter
		boolean applyFilterToClassName = false;
		if(config != null) {
			applyFilterToClassName = config.isApplyRegExPackage();
			if(config.getFilterRegExPath() != null)
				filterPath = config.getFilterRegExPath();
		}

		int filterApplyLevel = 500;
		if(config != null)
			config.getFilterApplyLevel();

		TxConfig txConfig = new TxConfig(transactionOnByDefault);
		if(applyFilterToClassName) {
			bldr.addPackageFilter(filterPath, TransactionFilter.class, txConfig, FilterPortType.ALL_FILTER, filterApplyLevel);
		} else {
			bldr.addFilter(filterPath, TransactionFilter.class, txConfig, FilterPortType.ALL_FILTER, filterApplyLevel);
		}
	}

}
