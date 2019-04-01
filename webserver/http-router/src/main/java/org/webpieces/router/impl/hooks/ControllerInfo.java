package org.webpieces.router.impl.hooks;

import com.google.inject.Injector;

public class ControllerInfo {

	private Injector injector;

	public ControllerInfo(Injector injector) {
		this.injector = injector;
	}

	public Injector getInjector() {
		return injector;
	}

}
