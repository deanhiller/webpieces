package org.webpieces.devrouter.impl;

import java.util.List;

import javax.inject.Inject;

import org.webpieces.router.impl.FilterInfo;
import org.webpieces.router.impl.RouteMeta;
import org.webpieces.router.impl.hooks.MetaLoaderProxy;
import org.webpieces.router.impl.loader.AbstractLoader;
import org.webpieces.router.impl.loader.MetaLoader;
import org.webpieces.router.impl.loader.ResolvedMethod;

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
	public void loadControllerIntoMeta(RouteMeta meta, ResolvedMethod method,
			boolean isInitializingAllControllers) {
		if(isInitializingAllControllers)
			return; //skip on startup
		
		super.loadRouteImpl(meta, method);
	}

	@Override
	public void loadFiltersIntoMeta(RouteMeta m, List<FilterInfo<?>> filters, boolean isInitializingAllFilters) {
		if(isInitializingAllFilters)
			return; //skip on startup
		
		super.loadFiltersIntoMeta(m, filters);
	}
}
