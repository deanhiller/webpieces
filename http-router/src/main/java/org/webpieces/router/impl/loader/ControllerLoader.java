package org.webpieces.router.impl.loader;

import org.webpieces.router.impl.RouteMeta;

import com.google.inject.Injector;

public interface ControllerLoader {

	void loadControllerIntoMeta(RouteMeta meta, Injector injector, String controllerStr, String methodStr,
			boolean isInitializingAllControllers);
	
}
