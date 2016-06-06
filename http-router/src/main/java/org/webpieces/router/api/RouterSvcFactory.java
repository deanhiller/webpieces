package org.webpieces.router.api;

import org.webpieces.util.file.VirtualFile;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public abstract class RouterSvcFactory {
	
    protected RouterSvcFactory() {}

    public static RoutingService create(VirtualFile routersFile) {
    	return create(new HttpRouterConfig(routersFile, null));
    }
    
	public static RoutingService create(HttpRouterConfig config) {
		Injector injector = Guice.createInjector(new HttpRouterModule(config));
		RoutingService svc = injector.getInstance(RoutingService.class);
		return svc;	
	}

	protected abstract RoutingService createImpl(VirtualFile modules, Module overrideModule);
	
}
