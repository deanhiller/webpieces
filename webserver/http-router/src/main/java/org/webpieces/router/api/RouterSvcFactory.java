package org.webpieces.router.api;

import java.util.List;

import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.security.SecretKeyInfo;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public class RouterSvcFactory {
	
    protected RouterSvcFactory() {}

    public static RouterService create(VirtualFile routersFile) {
    	return create(
    			new RouterConfig().setMetaFile(routersFile)
    					.setSecretKey(SecretKeyInfo.generateForTest())
    					.setPortConfigCallback(() -> fetch()));
    }
    
	private static PortConfig fetch() {
		return new PortConfig(8080, 8443);
	}
	
	public static RouterService create(RouterConfig config) {
		config.setPortConfigCallback(() -> fetch());
		Injector injector = Guice.createInjector(new ProdRouterModule(config));
		RouterService svc = injector.getInstance(RouterService.class);
		return svc;	
	}

	public static List<Module> getModules(RouterConfig config) {
		config.setPortConfigCallback(() -> fetch());
		List<Module> modules = Lists.newArrayList(new ProdRouterModule(config));
		return modules;
	}
}
