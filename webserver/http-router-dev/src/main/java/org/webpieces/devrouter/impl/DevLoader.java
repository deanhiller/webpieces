package org.webpieces.devrouter.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.impl.hooks.MetaLoaderProxy;
import org.webpieces.router.impl.hooks.ServiceCreationInfo;
import org.webpieces.router.impl.loader.AbstractLoader;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.router.impl.loader.MetaLoader;
import org.webpieces.router.impl.loader.ResolvedMethod;
import org.webpieces.util.filters.Service;

import com.google.inject.Injector;

@Singleton
public class DevLoader extends AbstractLoader implements MetaLoaderProxy {

	private DevClassForName classLoader;

	@Inject
	public DevLoader(MetaLoader loader, DevClassForName classLoader) {
		super(loader);
		this.classLoader = classLoader;
	}
	
	protected Object createController(Injector injector, String controllerClassFullName) {
		Class<?> clazz = classLoader.clazzForName(controllerClassFullName);
		return injector.getInstance(clazz);
	}
	
	@Override
	public LoadedController loadControllerIntoMeta(Injector injector, ResolvedMethod method) {
		return super.loadRouteImpl(injector, method);
	}

	@Override
	public Service<MethodMeta, Action> createServiceFromFilters(ServiceCreationInfo info) {
		return super.createServiceFromFiltersImpl(info);
	}
}
