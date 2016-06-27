package org.webpieces.router.impl.loader;

import javax.inject.Inject;

import org.webpieces.router.impl.RouteMeta;

public class ControllerLoader {

	private MetaLoaderProxy loader;
	private ControllerResolver resolver;

	@Inject
	public ControllerLoader(MetaLoaderProxy loader, ControllerResolver resolver) {
		this.loader = loader;
		this.resolver = resolver;
	}
	
	/**
	 * isInitializingAllControllers is true if in process of initializing ALL controllers and false if just being called to
	 * initialize on controller
	 * 
	 * @param meta
	 * @param isInitializingAllControllers
	 */
	public void loadControllerIntoMetaObject(RouteMeta meta, boolean isInitializingAllControllers) {
		ResolvedMethod method = resolver.resolveControllerClassAndMethod(meta);
		
		//This is a hook for the dev server with auto-compile (if isInitializing, dev server skips this piece)
		//if not initializing, dev server does this piece.  Production does the opposite.
		loader.loadControllerIntoMeta(meta, method, isInitializingAllControllers);
	}
}
