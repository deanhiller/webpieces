package org.webpieces.router.impl.loader;

import org.webpieces.router.api.extensions.BodyContentBinder;

public class BinderAndLoader {

	private final MethodMetaAndController methodAndController;
	private final BodyContentBinder binder;

	public BinderAndLoader(MethodMetaAndController methodAndController, BodyContentBinder binder) {
		this.methodAndController = methodAndController;
		this.binder = binder;
	}

	public MethodMetaAndController getMetaAndController() {
		return methodAndController;
	}

	public BodyContentBinder getBinder() {
		return binder;
	}

}
