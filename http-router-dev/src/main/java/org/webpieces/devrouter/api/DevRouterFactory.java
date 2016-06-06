package org.webpieces.devrouter.api;

import org.webpieces.router.api.HttpRouterConfig;
import org.webpieces.router.api.HttpRouterModule;
import org.webpieces.router.api.RouterSvcFactory;
import org.webpieces.router.api.RoutingService;
import org.webpieces.util.file.VirtualFile;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

public class DevRouterFactory {
    protected DevRouterFactory() {}

    public static RoutingService create(VirtualFile routersFile) {
    	return create(new HttpRouterConfig(routersFile, null));
    }
    
	public static RoutingService create(HttpRouterConfig config) {
		
		Modules.override(RouterSvcFactory.getModules(config)).with(new DevModule());
		
		Injector injector = Guice.createInjector(new HttpRouterModule(config));
		RoutingService svc = injector.getInstance(RoutingService.class);
		return svc;	
	}
}
