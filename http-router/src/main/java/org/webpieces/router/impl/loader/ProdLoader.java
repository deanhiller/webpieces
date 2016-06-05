package org.webpieces.router.impl.loader;

import com.google.inject.Injector;

public class ProdLoader implements Loader {

	@Override
	public Class<?> clazzForName(String moduleName) {
		try {
			return Class.forName(moduleName);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Your clazz="+moduleName+" was not found on the classpath", e);
		}
	}
	@Override
	public Object createController(Injector injector, String controllerClassFullName,
			boolean isInitializingAllControllers) {
		if(!isInitializingAllControllers) {
			throw new IllegalStateException("This should not be called with false in prod because it should already be loaded during init phase");
		}
		
		Class<?> clazz = clazzForName(controllerClassFullName);
		return injector.getInstance(clazz);
	}

}
