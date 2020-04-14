package org.webpieces.router.api;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.google.inject.*;
import com.google.inject.Module;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.webpieces.util.cmdline2.Arguments;
import org.webpieces.util.cmdline2.CommandLineParser;
import org.webpieces.util.file.FileFactory;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.security.SecretKeyInfo;

import com.google.common.collect.Lists;

import io.micrometer.core.instrument.MeterRegistry;

import javax.inject.Named;
import javax.inject.Singleton;

public class RouterSvcFactory {

	public static final String APP_METRICS_KEY = "app.metrics";
	public static final String PLATFORM_METRICS_KEY = "platform.metrics";

    protected RouterSvcFactory() {}

    public static RouterService create(MeterRegistry metrics, VirtualFile routersFile) {
    		File baseWorkingDir = FileFactory.getBaseWorkingDir();
    		Arguments arguments = new CommandLineParser().parse();
    		RouterConfig config = new RouterConfig(baseWorkingDir)
    									.setMetaFile(routersFile)
    									.setSecretKey(SecretKeyInfo.generateForTest());
    		RouterService svc = create(metrics, config);
    		svc.configure(arguments);
    		arguments.checkConsumedCorrectly();
    		return svc;
    }
    
	public static RouterService create(MeterRegistry metrics, RouterConfig config) {
		Injector injector = Guice.createInjector(getModules(metrics, config));
		RouterService svc = injector.getInstance(RouterService.class);
		return svc;	
	}


	public static List<Module> getModules(MeterRegistry metrics, RouterConfig config) {
		List<Module> modules = new ArrayList<Module>();
		modules.addAll(getModules(config));
		modules.add(new MetricModule(metrics));
		return modules;
	}
	
	public static List<Module> getModules(RouterConfig config) {
		List<Module> modules = Lists.newArrayList(new ProdRouterModule(config, new EmptyPortConfigLookup()));
		return modules;
	}
	
	private static class MetricModule implements Module {

		private MeterRegistry metrics;

		public MetricModule(MeterRegistry metrics) {
			this.metrics = metrics;
		}

		@Singleton
		@Provides
		@Named(RouterSvcFactory.PLATFORM_METRICS_KEY)
		public MeterRegistry providePlatformMetrics(MeterRegistry base) {
			//install a default for platform metrics...
			return base;
		}

		@Singleton
		@Provides
		@Named(RouterSvcFactory.APP_METRICS_KEY)
		public MeterRegistry provideAppMetrics(MeterRegistry base) {
			return base;
		}

		@Override
		public void configure(Binder binder) {
			binder.bind(MeterRegistry.class).toInstance(metrics);
		}
		
	}
}
