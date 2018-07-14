package org.webpieces.plugins.json;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.routing.BackendRoutes;
import org.webpieces.router.api.routing.Router;

public class InstallSslCertRoutes extends BackendRoutes {

	public InstallSslCertRoutes() {
	}
	
	@Override
	protected void configure() {
		addRoute(HttpMethod.GET, "/secure/sslsetup", "InstallSslCertController.renderSslSetup", InstallSslCertRouteId.INSTALL_SSL_SETUP);

		Router httpRouter = getScopedRouter("public", false);
		httpRouter.addRoute(HttpMethod.GET, "/letsencrypt/{token}", "InstallSslCertController.renderToken", InstallSslCertRouteId.TOKEN_VERIFY_ROUTE);
	}

}
