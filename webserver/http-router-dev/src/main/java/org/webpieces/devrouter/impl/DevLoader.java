package org.webpieces.devrouter.impl;

import javax.inject.Inject;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.impl.dto.MethodMeta;
import org.webpieces.router.impl.hooks.ControllerInfo;
import org.webpieces.router.impl.hooks.MetaLoaderProxy;
import org.webpieces.router.impl.hooks.ServiceCreationInfo;
import org.webpieces.router.impl.loader.AbstractLoader;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.router.impl.loader.MetaLoader;
import org.webpieces.router.impl.loader.ResolvedMethod;
import org.webpieces.util.filters.Service;

import com.google.inject.Injector;

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
	public LoadedController loadControllerIntoMeta(ControllerInfo meta, ResolvedMethod method,
			boolean isInitializingAllControllers) {
		if(isInitializingAllControllers)
			return null; //skip on startup
		
		return super.loadRouteImpl(meta, method);
	}

	@Override
	public Service<MethodMeta, Action> createServiceFromFilters(ServiceCreationInfo info, boolean isInitializingAllFilters) {
		if(isInitializingAllFilters)
			return null; //skip on startup
		
		return super.createServiceFromFiltersImpl(info);
	}
}
