package org.webpieces.devrouter.api;

import java.io.File;

import org.webpieces.compiler.api.CompileConfig;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.api.RouterService;
import org.webpieces.router.api.RouterServiceFactory;
import org.webpieces.router.api.TemplateApi;
import org.webpieces.util.cmdline2.Arguments;
import org.webpieces.util.cmdline2.ArgumentsCheck;
import org.webpieces.util.cmdline2.CommandLineParser;
import org.webpieces.util.file.FileFactory;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.security.SecretKeyInfo;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

import io.micrometer.core.instrument.MeterRegistry;

public class DevRouterFactory {
    protected DevRouterFactory() {}

    public static RouterService create(String testName, MeterRegistry metrics, VirtualFile routersFile, CompileConfig compileConfig, TemplateApi templateApi, Module ... routerOverrides) {
		File baseWorkingDir = FileFactory.getBaseWorkingDir();
		ArgumentsCheck arguments = new CommandLineParser().parse();
		RouterConfig config = new RouterConfig(baseWorkingDir, testName)
									.setMetaFile(routersFile)
									.setSecretKey(SecretKeyInfo.generateForTest());
    	RouterService svc = create(metrics, config, compileConfig, templateApi);
    	svc.configure(arguments);
    	arguments.checkConsumedCorrectly();
    	return svc;
    }
    
	public static RouterService create(MeterRegistry metrics, RouterConfig config, CompileConfig compileConfig, TemplateApi templateApi) {
		Module devModules = Modules.override(RouterServiceFactory.getModules(metrics, config, templateApi)).with(new DevRouterModule(compileConfig));
		
		Injector injector = Guice.createInjector(devModules);
		RouterService svc = injector.getInstance(RouterService.class);
		return svc;	
	}
}
