package org.webpieces.plugins.backend;

import java.util.List;
import java.util.function.Supplier;

import org.webpieces.plugins.backend.login.BackendLoginRoutes;
import org.webpieces.router.api.plugins.Plugin;
import org.webpieces.router.api.routes.Routes;

import com.google.common.collect.Lists;
import com.google.inject.Module;

public class BackendPlugin implements Plugin {

	public static final String BACKEND_PORT_KEY = "backend.port";
	private Supplier<Boolean> isUsePluginAssets;

	public BackendPlugin(BackendConfig config) {
		super();
		isUsePluginAssets = config.getArguments().consumeDoesExist(BACKEND_PORT_KEY, "IF backend host exists, backend plugin will turn on serving up necessary css/js/image files");
	}
	
	@Override
	public List<Module> getGuiceModules() {
		return Lists.newArrayList(new BackendModule());
	}

	@Override
	public List<Routes> getRouteModules() {
		return Lists.newArrayList(
			new BackendLoginRoutes(isUsePluginAssets, "/org/webpieces/plugins/backend/login/BackendLoginController", 
					BackendRoutes.BACKEND_ROUTE, BackendRoutes.BACKEND_ROUTE+"/secure.*")
		);
	}

}
