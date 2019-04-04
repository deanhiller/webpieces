package org.webpieces.router.impl.loader;

import org.webpieces.router.api.extensions.BodyContentBinder;

public class BinderAndLoader {

	private final LoadedController loadedController;
	private final BodyContentBinder binder;

	public BinderAndLoader(LoadedController loadedController, BodyContentBinder binder) {
		this.loadedController = loadedController;
		this.binder = binder;
	}

	public LoadedController getLoadedController() {
		return loadedController;
	}

	public BodyContentBinder getBinder() {
		return binder;
	}

}
