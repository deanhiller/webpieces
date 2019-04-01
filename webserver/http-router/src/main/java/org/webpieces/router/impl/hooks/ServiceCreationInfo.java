package org.webpieces.router.impl.hooks;

import java.util.List;

import org.webpieces.router.impl.FilterInfo;

import com.google.inject.Injector;

public class ServiceCreationInfo {

	private final Injector injector;
	private final List<FilterInfo<?>> filterInfos;

	public ServiceCreationInfo(Injector injector, List<FilterInfo<?>> filterInfos) {
		super();
		this.injector = injector;
		this.filterInfos = filterInfos;
	}	
	public Injector getInjector() {
		return injector;
	}

	public List<FilterInfo<?>> getFilterInfos() {
		return filterInfos;
	}

}
