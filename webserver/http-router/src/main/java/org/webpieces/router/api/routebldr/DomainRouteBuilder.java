package org.webpieces.router.api.routebldr;

public interface DomainRouteBuilder {

	/**
	 * 	NOTE: This is EXACTLY the same and just a delegate method of
	 * 	getBuilderForAllOtherDomains().getBldrForAllOtherContentTypes()
	 *
	 * Gets the RouteBuilder you can add routes to that will match on all domains not specified when
	 * creating a DomainScopedRouteBuilder.  If you create a DomainScopedRouteBuilder, those domains
	 * are then excluded and no routes built with this builder will match when requests come from the
	 * DomainScopedRouteBuilder domains.
	 */
	RouteBuilder getAllDomainsRouteBuilder();

	/**
	 * 90% of the time, using this is what you want!!!
	 */
	AllContentTypesBuilder getBuilderForAllOtherDomains();
	/**
	 * DO NOT USE this unless you know what you are doing.  This is for advanced users.
	 */
	AllContentTypesBuilder getDomainScopedBuilder(String domainRegEx);
	
	AllContentTypesBuilder getBackendBuilder();
	
}
