package org.webpieces.router.impl.hooks;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.dto.MethodMeta;
import org.webpieces.router.impl.RouteMeta;
import org.webpieces.router.impl.loader.ResolvedMethod;
import org.webpieces.util.filters.Service;

public interface MetaLoaderProxy {

	void loadControllerIntoMeta(RouteMeta meta, ResolvedMethod method,
			boolean isInitializingAllControllers);

	void loadFiltersIntoMeta(RouteMeta m, boolean isInitializingAllFilters);

	Service<MethodMeta, Action> createServiceFromFilters(RouteMeta m);
	
}
