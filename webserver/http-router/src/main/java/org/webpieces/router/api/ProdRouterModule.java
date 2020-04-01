package org.webpieces.router.api;

import org.webpieces.ctx.api.ApplicationContext;
import org.webpieces.router.impl.ApplicationContextImpl;
import org.webpieces.router.impl.ProdRouterService;
import org.webpieces.router.impl.compression.CompressionCacheSetup;
import org.webpieces.router.impl.compression.ProdCompressionCacheSetup;
import org.webpieces.router.impl.hooks.ClassForName;
import org.webpieces.router.impl.hooks.MetaLoaderProxy;
import org.webpieces.router.impl.loader.ProdClassForName;
import org.webpieces.router.impl.loader.ProdLoader;
import org.webpieces.router.impl.mgmt.GuiceWebpiecesListener;
import org.webpieces.router.impl.mgmt.ManagedBeanMeta;
import org.webpieces.router.impl.routeinvoker.ProdRouteInvoker;
import org.webpieces.router.impl.routeinvoker.RouteInvoker;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.matcher.Matchers;

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
		binder.bind(RouterService.class).to(ProdRouterService.class).asEagerSingleton();
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
		binder.bind(ApplicationContext.class).toInstance(new ApplicationContextImpl());
	}
	
}
