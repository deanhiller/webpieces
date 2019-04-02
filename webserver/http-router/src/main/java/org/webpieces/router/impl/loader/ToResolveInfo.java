package org.webpieces.router.impl.loader;

import com.google.inject.Injector;

public class ToResolveInfo {

	private final Injector injector;
	private final String controllerMethodString;
	private final String packageContext;

	public ToResolveInfo(Injector injector, String controllerMethodString, String packageContext) {
		this.injector = injector;
		this.controllerMethodString = controllerMethodString;
		this.packageContext = packageContext;
	}

	public String getControllerMethodString() {
		return controllerMethodString;
	}

	public String getPackageContext() {
		return packageContext;
	}

	public Injector getInjector() {
		return injector;
	}

}
