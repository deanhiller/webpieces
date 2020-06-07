package org.webpieces.router.impl.routebldr;

import java.util.List;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.util.filters.Service;

import com.google.inject.Injector;

public class FilterCreationMeta {

	private Injector injector;
	private List<FilterInfo<?>> filters;
	private Service<MethodMeta, Action> svc;

	public FilterCreationMeta(Injector injector, List<FilterInfo<?>> filters, Service<MethodMeta, Action> svc) {
		this.injector = injector;
		this.filters = filters;
		this.svc = svc;
	}

	public Injector getInjector() {
		return injector;
	}

	public List<FilterInfo<?>> getFilters() {
		return filters;
	}

	public Service<MethodMeta, Action> getService() {
		return svc;
	}

}
