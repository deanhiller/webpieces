package org.webpieces.router.api;

public class EmptyPortConfigLookup implements PortConfigLookup {

	@Override
	public PortConfig getPortConfig() {
		return null;
	}

}
