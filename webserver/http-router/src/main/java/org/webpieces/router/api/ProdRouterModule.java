package org.webpieces.router.api;

import org.webpieces.router.impl.ProdRouteInvoker;
import org.webpieces.router.impl.ProdRouterService;
import org.webpieces.router.impl.RouteInvoker;
import org.webpieces.router.impl.compression.CompressionCacheSetup;
import org.webpieces.router.impl.compression.ProdCompressionCacheSetup;
import org.webpieces.router.impl.hooks.ClassForName;
import org.webpieces.router.impl.hooks.MetaLoaderProxy;
import org.webpieces.router.impl.loader.ProdClassForName;
import org.webpieces.router.impl.loader.ProdLoader;
import org.webpieces.router.impl.mgmt.GuiceWebpiecesListener;
import org.webpieces.router.impl.mgmt.ManagedBeanMeta;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.matcher.Matchers;

public class ProdRouterModule implements Module {

	private RouterConfig config;

	public ProdRouterModule(RouterConfig config) {
		this.config = config;
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
	}
	
}
