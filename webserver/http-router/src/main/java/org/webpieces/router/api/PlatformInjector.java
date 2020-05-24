package org.webpieces.router.api;

import com.google.inject.Injector;

public class PlatformInjector {

	private Injector injector;

	public PlatformInjector(Injector injector) {
		this.injector = injector;
	}

	public Injector get() {
		return injector;
	}

}
