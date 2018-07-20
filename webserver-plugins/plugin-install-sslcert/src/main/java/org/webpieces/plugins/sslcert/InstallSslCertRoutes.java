package org.webpieces.plugins.sslcert;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.plugins.backend.BackendRoutes;
import org.webpieces.router.api.routing.Router;

public class InstallSslCertRoutes extends BackendRoutes {

	public static final String SETUP_PATH2 = "/sslsetup";
	public static final String BACKEND_SETUP_PATH = "/secure"+SETUP_PATH2;
	
	public InstallSslCertRoutes() {
	}
	
	@Override
	protected void configure() {
		Router https = getScopedRouter("/secure", true);
		https.addRoute(HttpMethod.GET,  SETUP_PATH2, "InstallSslCertController.sslSetup", InstallSslCertRouteId.INSTALL_SSL_SETUP);
		https.addRoute(HttpMethod.POST, "/postEmail", "InstallSslCertController.postStartSslInstall", InstallSslCertRouteId.POST_START_SSL_INSTALL);
		https.addRoute(HttpMethod.GET,  "/step2",  "InstallSslCertController.step2", InstallSslCertRouteId.STEP2);
		https.addRoute(HttpMethod.POST, "/postStep2", "InstallSslCertController.postStep2", InstallSslCertRouteId.POST_STEP2);
		
		https.addRoute(HttpMethod.GET,  "/maintainssl", "InstallSslCertController.maintainSsl", InstallSslCertRouteId.MAINTAIN_SSL);

		//route taken from https://shredzone.org/maven/acme4j/challenge/http-01.html AND we made it https and http  
		baseRouter.addRoute(HttpMethod.GET, "/.well-known/acme-challenge/{token}", "InstallSslCertController.renderToken", InstallSslCertRouteId.TOKEN_VERIFY_ROUTE);
	}

}
