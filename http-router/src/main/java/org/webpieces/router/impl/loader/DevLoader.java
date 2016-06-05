package org.webpieces.router.impl.loader;

import org.webpieces.compiler.api.CompileOnDemand;

import com.google.inject.Injector;

public class DevLoader implements Loader {

	private CompileOnDemand compileOnDemand;

	public DevLoader(CompileOnDemand compile) {
		this.compileOnDemand = compile;
	}
	
	@Override
	public Class<?> clazzForName(String moduleName) {
		return compileOnDemand.loadClass(moduleName);
	}

	@Override
	public Object createController(Injector injector, String controllerClassFullName,
			boolean isInitializingAllControllers) {
		if(isInitializingAllControllers)
			return null;
		
		Class<?> clazz = clazzForName(controllerClassFullName);
		return injector.getInstance(clazz);
	}
}
