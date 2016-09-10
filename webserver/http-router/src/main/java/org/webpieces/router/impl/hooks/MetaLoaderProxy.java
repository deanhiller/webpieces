package org.webpieces.router.impl.hooks;

import java.util.List;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.dto.MethodMeta;
import org.webpieces.router.impl.FilterInfo;
import org.webpieces.router.impl.RouteMeta;
import org.webpieces.router.impl.loader.ResolvedMethod;
import org.webpieces.util.filters.Service;

public interface MetaLoaderProxy {

	void loadControllerIntoMeta(RouteMeta meta, ResolvedMethod method,
			boolean isInitializingAllControllers);

	void loadFiltersIntoMeta(RouteMeta m, List<FilterInfo<?>> filters, boolean isInitializingAllFilters);

	Service<MethodMeta, Action> createServiceFromFilters(RouteMeta m, List<FilterInfo<?>> filterInfos);
	
}
