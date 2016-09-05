package org.webpieces.devrouter.api;

import org.webpieces.compiler.api.CompileConfig;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.api.RouterSvcFactory;
import org.webpieces.router.api.RoutingService;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.security.SecretKeyInfo;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

public class DevRouterFactory {
    protected DevRouterFactory() {}

    public static RoutingService create(VirtualFile routersFile, CompileConfig compileConfig) {
    	return create(new RouterConfig().setMetaFile(routersFile).setSecretKey(SecretKeyInfo.generateForTest()), compileConfig);
    }
    
	public static RoutingService create(RouterConfig config, CompileConfig compileConfig) {
		
		Module devModules = Modules.override(RouterSvcFactory.getModules(config)).with(new DevRouterModule(compileConfig));
		
		Injector injector = Guice.createInjector(devModules);
		RoutingService svc = injector.getInstance(RoutingService.class);
		return svc;	
	}
}
