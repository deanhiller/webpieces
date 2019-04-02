package org.webpieces.router.impl.loader;

import java.util.List;

import org.webpieces.router.impl.FilterInfo;

import com.google.inject.Injector;

public class InjectorAndFilters {

	private final Injector injector;
	private final List<FilterInfo<?>> filters;

	public InjectorAndFilters(Injector injector, List<FilterInfo<?>> filters) {
		this.injector = injector;
		this.filters = filters;
	}
	
	public Injector getInjector() {
		return injector;
	}

	public List<FilterInfo<?>> getFilters() {
		return filters;
	}

	
}
