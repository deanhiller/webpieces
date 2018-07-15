package org.webpieces.plugins.sslcert;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.plugins.backend.BackendRoutes;
import org.webpieces.router.api.routing.Router;

public class InstallSslCertRoutes extends BackendRoutes {

	public static final String SETUP_PATH = "/secure/sslsetup";
	
	public InstallSslCertRoutes() {
	}
	
	@Override
	protected void configure() {
		addRoute(HttpMethod.GET, SETUP_PATH, "InstallSslCertController.sslSetup", InstallSslCertRouteId.INSTALL_SSL_SETUP);

		Router httpRouter = getScopedRouter("/public", false);
		httpRouter.addRoute(HttpMethod.GET, "/letsencrypt/{token}", "InstallSslCertController.renderToken", InstallSslCertRouteId.TOKEN_VERIFY_ROUTE);
	}

}
