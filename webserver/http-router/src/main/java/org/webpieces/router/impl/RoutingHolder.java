package org.webpieces.router.impl;

import org.webpieces.router.api.routing.ReverseRouteLookup;
import org.webpieces.router.impl.model.R1RouterBuilder;

public class RoutingHolder {

	private R1RouterBuilder routerBuilder;
	private ReverseRouteLookup reverseRouteLookup;

	public R1RouterBuilder getRouterBuilder() {
		return routerBuilder;
	}

	public void setRouterBuilder(R1RouterBuilder routerBuilder) {
		this.routerBuilder = routerBuilder;
	}

	public void setReverseRouteLookup(ReverseRouteLookup reverseRouteLookup) {
		this.reverseRouteLookup = reverseRouteLookup;
	}

	public ReverseRouteLookup getReverseRouteLookup() {
		return reverseRouteLookup;
	}

}
