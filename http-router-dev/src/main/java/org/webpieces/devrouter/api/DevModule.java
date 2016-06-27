package org.webpieces.devrouter.api;

import org.webpieces.compiler.api.CompileConfig;
import org.webpieces.compiler.api.CompileOnDemand;
import org.webpieces.compiler.api.CompileOnDemandFactory;
import org.webpieces.devrouter.impl.DevClassForName;
import org.webpieces.devrouter.impl.DevLoader;
import org.webpieces.devrouter.impl.DevRoutingService;
import org.webpieces.router.api.RoutingService;
import org.webpieces.router.impl.loader.ClassForName;
import org.webpieces.router.impl.loader.ControllerLoader;

import com.google.inject.Binder;
import com.google.inject.Module;

public class DevModule implements Module {
	
	private CompileConfig config;

	public DevModule(CompileConfig config) {
		this.config = config;
	}

	@Override
	public void configure(Binder binder) {
		binder.bind(RoutingService.class).to(DevRoutingService.class).asEagerSingleton();;
		binder.bind(ControllerLoader.class).to(DevLoader.class).asEagerSingleton();
		binder.bind(ClassForName.class).to(DevClassForName.class).asEagerSingleton();
		
		CompileOnDemand onDemand = CompileOnDemandFactory.createCompileOnDemand(config);
		binder.bind(CompileOnDemand.class).toInstance(onDemand);
	}

}
