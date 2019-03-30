package org.webpieces.router.impl.loader;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.dto.MethodMeta;
import org.webpieces.router.impl.FilterInfo;
import org.webpieces.router.impl.RouteMeta;
import org.webpieces.router.impl.hooks.MetaLoaderProxy;
import org.webpieces.util.filters.Service;

@Singleton
public class ControllerLoader {

	private MetaLoaderProxy loader;
	private ControllerResolver resolver;

	@Inject
	public ControllerLoader(MetaLoaderProxy loader, ControllerResolver resolver) {
		this.loader = loader;
		this.resolver = resolver;
	}
	
	/**
	 * isInitializingAllControllers is true if in process of initializing ALL controllers and false if just being called to
	 * initialize on controller
	 * 
	 * @param meta
	 * @param isInitializingAllControllers
	 */
	public void loadControllerIntoMetaObject(RouteMeta meta, boolean isInitializingAllControllers) {
		ResolvedMethod method = resolver.resolveControllerClassAndMethod(meta);
		
		//This is a hook for the dev server with auto-compile (if isInitializing, dev server skips this piece)
		//if not initializing, dev server does this piece.  Production does the opposite.
		loader.loadControllerIntoMeta(meta, method, isInitializingAllControllers);
	}

	public void loadFiltersIntoMeta(RouteMeta m, boolean isInitializingAllFilters) {
		loader.loadFiltersIntoMeta(m, isInitializingAllFilters);
	}

	public Service<MethodMeta, Action> createNotFoundService(RouteMeta m, List<FilterInfo<?>> filterInfos) {
		return loader.createServiceFromFilters(m, filterInfos);
	}
}
