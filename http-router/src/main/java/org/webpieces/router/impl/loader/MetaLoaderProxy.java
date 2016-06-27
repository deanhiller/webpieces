package org.webpieces.router.impl.loader;

import org.webpieces.router.impl.RouteMeta;

public interface MetaLoaderProxy {

	void loadControllerIntoMeta(RouteMeta meta, String controllerStr, String methodStr,
			boolean isInitializingAllControllers);
	
}
