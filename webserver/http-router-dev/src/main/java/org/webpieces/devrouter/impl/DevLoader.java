package org.webpieces.devrouter.impl;

import javax.inject.Inject;

import org.webpieces.router.impl.RouteMeta;
import org.webpieces.router.impl.loader.MetaLoaderProxy;
import org.webpieces.router.impl.loader.ResolvedMethod;
import org.webpieces.router.impl.loader.MetaLoader;

import com.google.inject.Injector;

public class DevLoader implements MetaLoaderProxy {

	private MetaLoader loader;
	private DevClassForName classLoader;

	@Inject
	public DevLoader(MetaLoader loader, DevClassForName classLoader) {
		this.loader = loader;
		this.classLoader = classLoader;
	}
	
	private Object createController(Injector injector, String controllerClassFullName) {
		Class<?> clazz = classLoader.clazzForName(controllerClassFullName);
		return injector.getInstance(clazz);
	}

	@Override
	public void loadControllerIntoMeta(RouteMeta meta, ResolvedMethod method,
			boolean isInitializingAllControllers) {
		if(isInitializingAllControllers)
			return;
		
		String controllerStr = method.getControllerStr();
		String methodStr = method.getMethodStr();
		
		Injector injector = meta.getInjector();
		Object controllerInst = createController(injector, controllerStr);
		loader.loadInstIntoMeta(meta, controllerInst, methodStr);		
	}
}
