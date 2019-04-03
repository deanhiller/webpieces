package org.webpieces.router.impl;

import org.webpieces.router.api.plugins.ReverseRouteLookup;
import org.webpieces.router.impl.routers.DomainRouter;

public class RoutingHolder {

	private ReverseRouteLookup reverseRouteLookup;
	private DomainRouter domainRouter;

	public void setReverseRouteLookup(ReverseRouteLookup reverseRouteLookup) {
		this.reverseRouteLookup = reverseRouteLookup;
	}

	
	public ReverseRouteLookup getReverseRouteLookup() {
		return reverseRouteLookup;
	}
	
	public DomainRouter getDomainRouter() {
		return domainRouter;
	}


	public void setDomainRouter(DomainRouter domainRouter) {
		this.domainRouter = domainRouter;
	}

}
