package org.webpieces.router.api;

import java.util.List;

import org.webpieces.util.file.VirtualFile;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public class RouterSvcFactory {
	
    protected RouterSvcFactory() {}

    public static RoutingService create(VirtualFile routersFile) {
    	return create(new HttpRouterConfig().setMetaFile(routersFile));
    }
    
	public static RoutingService create(HttpRouterConfig config) {
		Injector injector = Guice.createInjector(new ProdRouterModule(config));
		RoutingService svc = injector.getInstance(RoutingService.class);
		return svc;	
	}

	public static List<Module> getModules(HttpRouterConfig config) {
		List<Module> modules = Lists.newArrayList(new ProdRouterModule(config));
		return modules;
	}
}
