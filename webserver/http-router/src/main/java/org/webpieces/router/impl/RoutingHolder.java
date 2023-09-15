package org.webpieces.router.impl;

import javax.inject.Singleton;

import org.webpieces.router.api.plugins.ReverseRouteLookup;
import org.webpieces.router.impl.routers.BRouter;

@Singleton
public class RoutingHolder {

	private ReverseRouteLookup reverseRouteLookup;
	private BRouter domainRouter;

	public void setReverseRouteLookup(ReverseRouteLookup reverseRouteLookup) {
		this.reverseRouteLookup = reverseRouteLookup;
	}

	
	public ReverseRouteLookup getReverseRouteLookup() {
		if(reverseRouteLookup == null)
			throw new IllegalStateException("You are calling this too early(perhaps during guice construction) and it needs to be called later in the lifecycle post guice construction");
		return reverseRouteLookup;
	}
	
	public BRouter getDomainRouter() {
		if(reverseRouteLookup == null)
			throw new IllegalStateException("You are calling this too early(perhaps during guice construction) and it needs to be called later in the lifecycle post guice construction");

		return domainRouter;
	}


	public void setDomainRouter(BRouter domainRouter) {
		this.domainRouter = domainRouter;
	}

}
