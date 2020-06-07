package org.webpieces.plugin.secure.sslcert;

import static org.webpieces.router.api.routes.Port.BOTH;
import static org.webpieces.router.api.routes.Port.HTTPS;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.plugin.backend.BackendRoutes;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routebldr.ScopedRouteBuilder;

public class InstallSslCertRoutes extends BackendRoutes {

	public InstallSslCertRoutes() {
	}
	
	@Override
	protected void configure(RouteBuilder baseBldr, ScopedRouteBuilder scopedBldr) {
		ScopedRouteBuilder subRouter = baseBldr.getScopedRouteBuilder("/@sslcert");
		subRouter.addRoute(HTTPS, HttpMethod.GET,  "", "InstallSslCertController.sslSetup", InstallSslCertRouteId.INSTALL_SSL_SETUP);
		subRouter.addRoute(HTTPS, HttpMethod.POST, "/postEmail", "InstallSslCertController.postStartSslInstall", InstallSslCertRouteId.POST_START_SSL_INSTALL);
		subRouter.addRoute(HTTPS, HttpMethod.GET,  "/step2",  "InstallSslCertController.step2", InstallSslCertRouteId.STEP2);
		subRouter.addRoute(HTTPS, HttpMethod.POST, "/postStep2", "InstallSslCertController.postStep2", InstallSslCertRouteId.POST_STEP2);
		
		subRouter.addRoute(HTTPS, HttpMethod.GET,  "/maintainssl", "InstallSslCertController.maintainSsl", InstallSslCertRouteId.MAINTAIN_SSL);

		//route taken from https://shredzone.org/maven/acme4j/challenge/http-01.html AND we made it https and http  
		baseBldr.addRoute(BOTH, HttpMethod.GET, "/.well-known/acme-challenge/{token}", "InstallSslCertController.renderToken", InstallSslCertRouteId.TOKEN_VERIFY_ROUTE);
	}

}
