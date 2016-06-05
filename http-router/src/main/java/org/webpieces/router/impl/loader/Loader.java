package org.webpieces.router.impl.loader;

import com.google.inject.Injector;

public interface Loader {

	Class<?> clazzForName(String moduleName);

	Object createController(Injector injector, String controllerClassFullName, boolean isInitializingAllControllers);

}
