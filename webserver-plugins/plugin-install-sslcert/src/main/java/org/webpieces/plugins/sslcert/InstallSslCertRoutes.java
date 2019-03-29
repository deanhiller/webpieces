package org.webpieces.plugins.sslcert;

import static org.webpieces.router.api.routing.Port.BOTH;
import static org.webpieces.router.api.routing.Port.HTTPS;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.plugins.backend.BackendRoutes;
import org.webpieces.router.impl.model.bldr.RouteBuilder;
import org.webpieces.router.impl.model.bldr.ScopedRouteBuilder;

public class InstallSslCertRoutes extends BackendRoutes {

	public InstallSslCertRoutes() {
	}
	
	@Override
	protected void configure(RouteBuilder baseRouter, ScopedRouteBuilder scopedRouter) {
		ScopedRouteBuilder subRouter = scopedRouter.getScopedRouteBuilder("/secure");
		subRouter.addRoute(HTTPS, HttpMethod.GET,  "/sslsetup", "InstallSslCertController.sslSetup", InstallSslCertRouteId.INSTALL_SSL_SETUP);
		subRouter.addRoute(HTTPS, HttpMethod.POST, "/postEmail", "InstallSslCertController.postStartSslInstall", InstallSslCertRouteId.POST_START_SSL_INSTALL);
		subRouter.addRoute(HTTPS, HttpMethod.GET,  "/step2",  "InstallSslCertController.step2", InstallSslCertRouteId.STEP2);
		subRouter.addRoute(HTTPS, HttpMethod.POST, "/postStep2", "InstallSslCertController.postStep2", InstallSslCertRouteId.POST_STEP2);
		
		subRouter.addRoute(HTTPS, HttpMethod.GET,  "/maintainssl", "InstallSslCertController.maintainSsl", InstallSslCertRouteId.MAINTAIN_SSL);

		//route taken from https://shredzone.org/maven/acme4j/challenge/http-01.html AND we made it https and http  
		baseRouter.addRoute(BOTH, HttpMethod.GET, "/.well-known/acme-challenge/{token}", "InstallSslCertController.renderToken", InstallSslCertRouteId.TOKEN_VERIFY_ROUTE);
	}

}
