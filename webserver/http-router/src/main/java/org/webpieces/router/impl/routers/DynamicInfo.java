package org.webpieces.router.impl.routers;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.util.filters.Service;

/**
 * Information loaded on startup for production BUT for Development it
 * is loaded on the first request and re-loaded on next request ONLY IF there
 * are changes.  This ensures we only recompile what is needed for that request
 * keeping the development server relatively fast in spite of compiling
 * 
 */
public class DynamicInfo {

	private final LoadedController loadedController;
	private final Service<MethodMeta, Action> service;

	public DynamicInfo(LoadedController loadedController, Service<MethodMeta, Action> service) {
		super();
		this.loadedController = loadedController;
		this.service = service;
	}

	public LoadedController getLoadedController() {
		return loadedController;
	}

	public Service<MethodMeta, Action> getService() {
		return service;
	}

}
