package org.webpieces.router.impl.loader;

public class MethodMetaAndController {

	private ResolvedMethod methodMeta;
	private LoadedController loadedController;

	/**
	 * NOTE: In DevelopmentServer, loadedController will be null here on startup and filled in during requests to this same object.
	 * ResolvedMethod should never be null
	 */
	public MethodMetaAndController(ResolvedMethod methodMeta, LoadedController loadedController) {
		if(methodMeta == null)
			throw new IllegalArgumentException("methodMeta should not be null here, bug!!");
		this.methodMeta = methodMeta;
		this.loadedController = loadedController;
	}

	public ResolvedMethod getMethodMeta() {
		return methodMeta;
	}

	public LoadedController getLoadedController() {
		return loadedController;
	}
	
}
