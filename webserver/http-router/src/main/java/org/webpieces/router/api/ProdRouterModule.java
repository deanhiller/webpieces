package org.webpieces.router.api;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Named;
import javax.inject.Singleton;

import org.webpieces.ctx.api.Constants;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.TwoPools;
import org.webpieces.router.impl.AbstractRouterService;
import org.webpieces.router.impl.ProdRouterService;
import org.webpieces.router.impl.RouterServiceImpl;
import org.webpieces.router.impl.compression.CompressionCacheSetup;
import org.webpieces.router.impl.compression.ProdCompressionCacheSetup;
import org.webpieces.router.impl.hooks.ClassForName;
import org.webpieces.router.impl.hooks.MetaLoaderProxy;
import org.webpieces.router.impl.loader.ProdClassForName;
import org.webpieces.router.impl.loader.ProdLoader;
import org.webpieces.router.impl.mgmt.GuiceWebpiecesListener;
import org.webpieces.router.impl.mgmt.ManagedBeanMeta;
import org.webpieces.router.impl.proxyout.ProxyResponse;
import org.webpieces.router.impl.routeinvoker.ProdRouteInvoker;
import org.webpieces.router.impl.routeinvoker.RouteInvoker;
import org.webpieces.util.metrics.MetricsCreator;
import org.webpieces.util.threading.NamedThreadFactory;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.matcher.Matchers;

import io.micrometer.core.instrument.MeterRegistry;

public class ProdRouterModule implements Module {

	private final RouterConfig config;
	private final PortConfigLookup portLookup;

	public ProdRouterModule(RouterConfig config, PortConfigLookup portLookup) {
		this.config = config;
		this.portLookup = portLookup;
		if(portLookup == null)
			throw new IllegalArgumentException("portLookup cannot be null and was");
	}
	
	@Override
	public void configure(Binder binder) {
		binder.bind(RouterService.class).to(RouterServiceImpl.class).asEagerSingleton();
		binder.bind(AbstractRouterService.class).to(ProdRouterService.class).asEagerSingleton();;

		binder.bind(BufferPool.class).to(TwoPools.class).asEagerSingleton();

		//TODO(dhiller): rework this to remove the interface.
		binder.bind(ResponseStreamer.class).to(ProxyResponse.class);

		binder.bind(MetaLoaderProxy.class).to(ProdLoader.class).asEagerSingleton();
		binder.bind(RouteInvoker.class).to(ProdRouteInvoker.class).asEagerSingleton();
		binder.bind(ClassForName.class).to(ProdClassForName.class).asEagerSingleton();
		binder.bind(CompressionCacheSetup.class).to(ProdCompressionCacheSetup.class).asEagerSingleton();;
		
		binder.bind(RouterConfig.class).toInstance(config);
		
		//We write all meta for platform managed beans into ManagedBeanMeta such that 'any' plugin could
		//inject ManagedBeanMeta into itself to access and use all that meta data and wrap those beans to modify them
		//The properties plugin does this to expose platform beans as well as app beans
		ManagedBeanMeta beanMeta = new ManagedBeanMeta();
		binder.bind(ManagedBeanMeta.class).toInstance(beanMeta);
		binder.bindListener(Matchers.any(), new GuiceWebpiecesListener(beanMeta));
		
		binder.bind(PortConfigLookup.class).toInstance(portLookup);
	}
	
	@Provides
	@Singleton
	@Named(Constants.FILE_READ_EXECUTOR)
	public ExecutorService provideExecutor(MeterRegistry metrics) {
		String id = "fileReadPool";
		ExecutorService executor = Executors.newFixedThreadPool(10, new NamedThreadFactory(id));
		MetricsCreator.monitor(metrics, executor, id);
		return executor;
	}
	
}
