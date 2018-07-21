package org.webpieces.plugins.backend;

import java.util.List;

import org.webpieces.plugins.backend.login.BackendLoginRoutes;
import org.webpieces.router.api.routing.Plugin;
import org.webpieces.router.api.routing.Routes;

import com.google.common.collect.Lists;
import com.google.inject.Module;

public class BackendPlugin implements Plugin {

	public BackendPlugin(BackendConfig config) {
		super();
	}
	
	@Override
	public List<Module> getGuiceModules() {
		return Lists.newArrayList(new BackendModule());
	}

	@Override
	public List<Routes> getRouteModules() {
		return Lists.newArrayList(
			new BackendLoginRoutes("/org/webpieces/plugins/backend/login/BackendLoginController", 
					BackendRoutes.BACKEND_ROUTE, BackendRoutes.BACKEND_ROUTE+"/secure.*")
		);
	}

}
