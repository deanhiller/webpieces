package org.webpieces.router.impl.hooks;

import org.webpieces.router.impl.RouteMeta;
import org.webpieces.router.impl.loader.ResolvedMethod;

public interface MetaLoaderProxy {

	void loadControllerIntoMeta(RouteMeta meta, ResolvedMethod method,
			boolean isInitializingAllControllers);

	void loadFiltersIntoMeta(RouteMeta m, boolean isInitializingAllFilters);
	
}
