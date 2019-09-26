package org.webpieces.router.api;

import java.io.File;
import java.util.List;

import org.webpieces.util.cmdline2.Arguments;
import org.webpieces.util.cmdline2.CommandLineParser;
import org.webpieces.util.file.FileFactory;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.security.SecretKeyInfo;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public class RouterSvcFactory {
	
    protected RouterSvcFactory() {}

    public static RouterService create(VirtualFile routersFile) {
    		File baseWorkingDir = FileFactory.getBaseWorkingDir();
    		Arguments arguments = new CommandLineParser().parse();
    		RouterConfig config = new RouterConfig(baseWorkingDir)
    									.setMetaFile(routersFile)
    									.setSecretKey(SecretKeyInfo.generateForTest());
    		RouterService svc = create(config);
    		svc.configure(arguments);
    		arguments.checkConsumedCorrectly();
    		return svc;
    }
    
	public static RouterService create(RouterConfig config) {
		Injector injector = Guice.createInjector(new ProdRouterModule(config, new EmptyPortConfigLookup()));
		RouterService svc = injector.getInstance(RouterService.class);
		return svc;	
	}

	public static List<Module> getModules(RouterConfig config) {
		List<Module> modules = Lists.newArrayList(new ProdRouterModule(config, new EmptyPortConfigLookup()));
		return modules;
	}
}
