package org.webpieces.devrouter.api;

import org.webpieces.compiler.api.CompileConfig;
import org.webpieces.compiler.api.CompileOnDemand;
import org.webpieces.compiler.api.CompileOnDemandFactory;
import org.webpieces.devrouter.impl.DevRoutingService;
import org.webpieces.router.api.RoutingService;

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
		
		CompileOnDemand onDemand = CompileOnDemandFactory.createCompileOnDemand(config);
		binder.bind(CompileOnDemand.class).toInstance(onDemand);
	}

}
