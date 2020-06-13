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
		return reverseRouteLookup;
	}
	
	public BRouter getDomainRouter() {
		return domainRouter;
	}


	public void setDomainRouter(BRouter domainRouter) {
		this.domainRouter = domainRouter;
	}

}
