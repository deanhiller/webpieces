package org.webpieces.router.impl;

import org.webpieces.router.api.plugins.ReverseRouteLookup;
import org.webpieces.router.impl.routers.BRouter;

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
