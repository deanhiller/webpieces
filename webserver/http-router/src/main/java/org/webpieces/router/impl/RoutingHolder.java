package org.webpieces.router.impl;

import org.webpieces.router.api.plugins.ReverseRouteLookup;
import org.webpieces.router.impl.routers.BDomainRouter;

public class RoutingHolder {

	private ReverseRouteLookup reverseRouteLookup;
	private BDomainRouter domainRouter;

	public void setReverseRouteLookup(ReverseRouteLookup reverseRouteLookup) {
		this.reverseRouteLookup = reverseRouteLookup;
	}

	
	public ReverseRouteLookup getReverseRouteLookup() {
		return reverseRouteLookup;
	}
	
	public BDomainRouter getDomainRouter() {
		return domainRouter;
	}


	public void setDomainRouter(BDomainRouter domainRouter) {
		this.domainRouter = domainRouter;
	}

}
