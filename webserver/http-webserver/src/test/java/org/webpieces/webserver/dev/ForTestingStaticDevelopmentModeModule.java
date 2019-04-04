package org.webpieces.webserver.dev;

import org.webpieces.compiler.api.CompileOnDemand;
import org.webpieces.devrouter.impl.DevClassForName;
import org.webpieces.devrouter.impl.DevLoader;
import org.webpieces.devrouter.impl.DevRouteInvoker;
import org.webpieces.devrouter.impl.DevRoutingService;
import org.webpieces.router.api.RouterService;
import org.webpieces.router.impl.RouteInvoker;
import org.webpieces.router.impl.hooks.ClassForName;
import org.webpieces.router.impl.hooks.MetaLoaderProxy;

import com.google.inject.Binder;
import com.google.inject.Module;

public class ForTestingStaticDevelopmentModeModule implements Module {

	@Override
	public void configure(Binder binder) {
		binder.bind(RouterService.class).to(DevRoutingService.class).asEagerSingleton();
        binder.bind(MetaLoaderProxy.class).to(DevLoader.class).asEagerSingleton();
		binder.bind(ClassForName.class).to(DevClassForName.class).asEagerSingleton();
		
		binder.bind(CompileOnDemand.class).to(NoCompiling.class).asEagerSingleton();
		binder.bind(RouteInvoker.class).to(DevRouteInvoker.class).asEagerSingleton();
    }

	private static class NoCompiling implements CompileOnDemand {
		private ClassLoader cl = ForTestingStaticDevelopmentModeModule.class.getClassLoader();

		@Override
		public Class<?> loadClass(String clazzName) {
			try {
				return cl.loadClass(clazzName);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("could not load class", e);
			}
		}

		@Override
		public Class<?> loadClass(String name, boolean forceReload) {
			try {
				return cl.loadClass(name);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("could not load class", e);
			}
		}
	}
}
