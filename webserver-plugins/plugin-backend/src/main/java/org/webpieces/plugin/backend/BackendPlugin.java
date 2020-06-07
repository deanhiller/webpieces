package org.webpieces.plugin.backend;

import java.util.List;
import java.util.function.Supplier;

import org.webpieces.plugin.backend.login.BackendLoginRoutes;
import org.webpieces.router.api.plugins.Plugin;
import org.webpieces.router.api.routes.Routes;
import org.webpieces.util.cmdline2.Arguments;

import com.google.common.collect.Lists;
import com.google.inject.Module;

public class BackendPlugin implements Plugin {

	public static final String BACKEND_PORT_KEY = "backend.port";
	private Supplier<Boolean> isUsePluginAssets;

	public BackendPlugin(BackendConfig config) {
		isUsePluginAssets = () -> config.isUsePluginAssets();
	}
	
	public BackendPlugin(Arguments cmdLineArguments) {
		super();
		isUsePluginAssets = cmdLineArguments.createDoesExistArg(BACKEND_PORT_KEY, "IF backend host exists, backend plugin will turn on serving up necessary css/js/image files");
	}
	
	@Override
	public List<Module> getGuiceModules() {
		return Lists.newArrayList(new BackendModule());
	}

	@Override
	public List<Routes> getRouteModules() {
		return Lists.newArrayList(
			new BackendLoginRoutes(isUsePluginAssets, "/org/webpieces/plugin/backend/login/BackendLoginController",
					BackendRoutes.BACKEND_ROUTE, ".*plugin.secure.*")
		);
	}

}
