package org.webpieces.router.impl.hooks;

import java.util.List;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.impl.FilterInfo;
import org.webpieces.router.impl.loader.svc.MethodMeta;
import org.webpieces.util.filters.Service;

import com.google.inject.Injector;

public class ServiceCreationInfo {

	private final Injector injector;
	private final List<FilterInfo<?>> filterInfos;
	private Service<MethodMeta, Action> service;

	public ServiceCreationInfo(Injector injector, Service<MethodMeta, Action> service, List<FilterInfo<?>> filterInfos) {
		super();
		this.injector = injector;
		this.filterInfos = filterInfos;
		this.service = service;
	}	
	public Injector getInjector() {
		return injector;
	}

	public List<FilterInfo<?>> getFilterInfos() {
		return filterInfos;
	}
	public Service<MethodMeta, Action> getService() {
		return service;
	}

}
