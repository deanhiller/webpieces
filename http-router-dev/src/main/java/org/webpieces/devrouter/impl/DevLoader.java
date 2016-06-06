package org.webpieces.devrouter.impl;

import org.webpieces.compiler.api.CompileOnDemand;
import org.webpieces.router.impl.RouteMeta;
import org.webpieces.router.impl.loader.Loader;
import org.webpieces.router.impl.loader.MetaLoader;

import com.google.inject.Injector;

public class DevLoader implements Loader {

	private CompileOnDemand compileOnDemand;
	private MetaLoader loader;

	public DevLoader(CompileOnDemand compile, MetaLoader loader) {
		this.compileOnDemand = compile;
		this.loader = loader;
	}
	
	@Override
	public Class<?> clazzForName(String moduleName) {
		return compileOnDemand.loadClass(moduleName);
	}

	private Object createController(Injector injector, String controllerClassFullName) {
		Class<?> clazz = clazzForName(controllerClassFullName);
		return injector.getInstance(clazz);
	}

	@Override
	public void loadControllerIntoMeta(RouteMeta meta, Injector injector, String controllerStr, String methodStr,
			boolean isInitializingAllControllers) {
		if(isInitializingAllControllers)
			return;
		
		Object controllerInst = createController(injector, controllerStr);
		loader.loadInstIntoMeta(meta, controllerInst, methodStr);		
	}
}
