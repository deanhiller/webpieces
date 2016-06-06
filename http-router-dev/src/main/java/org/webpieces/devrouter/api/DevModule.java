package org.webpieces.devrouter.api;

import org.webpieces.devrouter.impl.DevRouterConfig;
import org.webpieces.router.impl.RouterConfig;

import com.google.inject.Binder;
import com.google.inject.Module;

public class DevModule implements Module {

	@Override
	public void configure(Binder binder) {
		binder.bind(RouterConfig.class).to(DevRouterConfig.class);
	}

}
