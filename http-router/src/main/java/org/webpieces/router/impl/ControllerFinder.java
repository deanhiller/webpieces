package org.webpieces.router.impl;

import javax.inject.Inject;

import org.webpieces.router.impl.loader.MetaLoaderProxy;

public class ControllerFinder {

	private MetaLoaderProxy loader;

	@Inject
	public ControllerFinder(MetaLoaderProxy loader) {
		this.loader = loader;
	}
	
	/**
	 * isInitializingAllControllers is true if in process of initializing ALL controllers and false if just being called to
	 * initialize on controller
	 * 
	 * @param meta
	 * @param isInitializingAllControllers
	 */
	public void loadControllerIntoMetaObject(RouteMeta meta, boolean isInitializingAllControllers) {
		Route r = meta.getRoute();
		String controllerAndMethod = r.getControllerMethodString();
		int lastIndex = controllerAndMethod.lastIndexOf(".");
		int fromBeginIndex = controllerAndMethod.indexOf(".");
		String methodStr = controllerAndMethod.substring(lastIndex+1);
		String controllerStr = controllerAndMethod.substring(0, lastIndex);
		if(lastIndex == fromBeginIndex) {
			controllerStr = meta.getPackageContext()+"."+controllerStr;
		}
		
		loader.loadControllerIntoMeta(meta, controllerStr, methodStr, isInitializingAllControllers);
	}
}
