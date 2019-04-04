package org.webpieces.router.impl.loader;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.impl.hooks.ClassForName;
import org.webpieces.router.impl.hooks.MetaLoaderProxy;
import org.webpieces.router.impl.hooks.ServiceCreationInfo;
import org.webpieces.router.impl.loader.svc.MethodMeta;
import org.webpieces.util.filters.Service;

import com.google.inject.Injector;

@Singleton
public class ProdLoader extends AbstractLoader implements MetaLoaderProxy {

	private ClassForName classForName;
	
	@Inject
	public ProdLoader(MetaLoader loader, ProdClassForName classLoader) {
		super(loader);
		this.classForName = classLoader;
	}
	
	protected Object createController(Injector injector, String controllerClassFullName) {
		Class<?> clazz = classForName.clazzForName(controllerClassFullName);
		return injector.getInstance(clazz);
	}

	@Override
	public LoadedController loadControllerIntoMeta(Injector injector, ResolvedMethod method,
			boolean isInitializingAllControllers) {
		try {
			return loadRouteImpl(injector, method);
		} catch(RuntimeException e) {
			String msg = "error=\n'"+e.getMessage()+"'\n"
					+"Check the stack trace for which client calls were calling addRoute or addXXXXRoute for which route is incorrect";
			throw new RuntimeException(msg, e);
		}
	}

	@Override
	public Service<MethodMeta, Action> createServiceFromFilters(ServiceCreationInfo info, boolean isInitializingAllFilters) {
		return super.createServiceFromFiltersImpl(info);
	}

}
