package org.webpieces.router.impl.routers;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.util.filters.Service;

/**
 * Information loaded on startup for production BUT for Development it
 * is loaded on the first request and re-loaded on next request ONLY IF there
 * are changes.  This ensures we only recompile what is needed for that request
 * keeping the development server relatively fast in spite of compiling
 * 
 */
public class DynamicInfo {

	private final Service<MethodMeta, Action> service;

	public DynamicInfo(Service<MethodMeta, Action> service) {
		super();
		this.service = service;
	}

	public Service<MethodMeta, Action> getService() {
		return service;
	}

}
