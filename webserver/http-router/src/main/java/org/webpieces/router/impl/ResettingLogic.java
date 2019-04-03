package org.webpieces.router.impl;

import com.google.inject.Injector;

public class ResettingLogic {

	private final ReverseRoutes reverseRoutes;
	private final Injector injector;

	public ResettingLogic(ReverseRoutes reverseRoutes, Injector injector) {
		this.reverseRoutes = reverseRoutes;
		this.injector = injector;
	}

	public Injector getInjector() {
		return injector;
	}

	public ReverseRoutes getReverseRoutes() {
		return reverseRoutes;
	}
	

}
