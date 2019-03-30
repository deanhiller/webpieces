package org.webpieces.router.impl.loader;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.router.impl.FilterInfo;
import org.webpieces.router.impl.RouteMeta;
import org.webpieces.router.impl.hooks.ClassForName;
import org.webpieces.router.impl.hooks.MetaLoaderProxy;

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
	public void loadControllerIntoMeta(RouteMeta meta, ResolvedMethod method,
			boolean isInitializingAllControllers) {
		try {
			loadRouteImpl(meta, method);
		} catch(RuntimeException e) {
			String msg = "error=\n'"+e.getMessage()+"'\n"
					+"Check the stack trace for which client calls were calling addRoute or addXXXXRoute for which route is incorrect";
			throw new RuntimeException(msg, e);
		}
	}

	@Override
	public void loadFiltersIntoMeta(RouteMeta m, boolean isInitializingAllFilters) {
		super.loadFiltersIntoMeta(m, m.getFilters());
	}

}
