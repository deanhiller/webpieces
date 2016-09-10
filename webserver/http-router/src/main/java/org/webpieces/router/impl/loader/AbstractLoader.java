package org.webpieces.router.impl.loader;

import java.util.ArrayList;
import java.util.List;

import org.webpieces.router.api.routing.RouteFilter;
import org.webpieces.router.impl.FilterInfo;
import org.webpieces.router.impl.RouteMeta;

import com.google.inject.Injector;

public abstract class AbstractLoader {

	private MetaLoader loader;

	public AbstractLoader(MetaLoader loader) {
		this.loader = loader;
	}

	protected void loadRouteImpl(RouteMeta meta, ResolvedMethod method) {
		String controllerStr = method.getControllerStr();
		String methodStr = method.getMethodStr();
		
		Injector injector = meta.getInjector();
		Object controllerInst = createController(injector, controllerStr);
		loader.loadInstIntoMeta(meta, controllerInst, methodStr);
	}

	protected abstract <T> RouteFilter<T> createFilterImpl(Injector injector, FilterInfo<T> info);

	protected abstract Object createController(Injector injector, String controllerStr);

	public void loadFiltersIntoMeta(RouteMeta meta) {
		List<FilterInfo<?>> filterInfos = meta.getFilters();
		
		Injector injector = meta.getInjector();
		List<RouteFilter<?>> filters = createFilters(injector, filterInfos);
		loader.loadFilters(meta, filters);
	}
	
	protected List<RouteFilter<?>> createFilters(Injector injector, List<FilterInfo<?>> filterInfos) {
		List<RouteFilter<?>> filters = new ArrayList<>();
		for(FilterInfo<?> info : filterInfos) {
			RouteFilter<?> filter = createFilter2(injector, info);
			filters.add(filter);
		}
		return filters;
	}
	
	protected <T> RouteFilter<T> createFilter2(Injector injector, FilterInfo<T> info) {
		RouteFilter<T> f = createFilterImpl(injector, info);
		f.initialize(info.getInitialConfig());
		return f;
	}

	
}
