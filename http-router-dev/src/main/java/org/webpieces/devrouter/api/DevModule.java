package org.webpieces.devrouter.api;

import org.webpieces.devrouter.impl.DevRoutingService;
import org.webpieces.router.api.RoutingService;

import com.google.inject.Binder;
import com.google.inject.Module;

public class DevModule implements Module {

	@Override
	public void configure(Binder binder) {
		binder.bind(RoutingService.class).to(DevRoutingService.class).asEagerSingleton();;
	}

}
