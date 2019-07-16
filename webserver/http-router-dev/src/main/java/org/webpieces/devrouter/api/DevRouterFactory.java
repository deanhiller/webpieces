package org.webpieces.devrouter.api;

import java.io.File;

import org.webpieces.compiler.api.CompileConfig;
import org.webpieces.router.api.EmptyPortConfigLookup;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.api.RouterService;
import org.webpieces.router.api.RouterSvcFactory;
import org.webpieces.util.cmdline2.Arguments;
import org.webpieces.util.cmdline2.CommandLineParser;
import org.webpieces.util.file.FileFactory;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.security.SecretKeyInfo;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

public class DevRouterFactory {
    protected DevRouterFactory() {}

    public static RouterService create(VirtualFile routersFile, CompileConfig compileConfig) {
		File baseWorkingDir = FileFactory.getBaseWorkingDir();
		Arguments arguments = new CommandLineParser().parse();
		RouterConfig config = new RouterConfig(baseWorkingDir)
									.setMetaFile(routersFile)
									.setSecretKey(SecretKeyInfo.generateForTest())
									.setPortLookupConfig(new EmptyPortConfigLookup());
    	RouterService svc = create(config, compileConfig);
    	svc.configure(arguments);
    	arguments.checkConsumedCorrectly();
    	return svc;
    }
    
	public static RouterService create(RouterConfig config, CompileConfig compileConfig) {
		
		Module devModules = Modules.override(RouterSvcFactory.getModules(config)).with(new DevRouterModule(compileConfig));
		
		Injector injector = Guice.createInjector(devModules);
		RouterService svc = injector.getInstance(RouterService.class);
		return svc;	
	}
}
