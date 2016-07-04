package org.webpieces.frontend.api;

import org.webpieces.nio.api.SSLEngineFactory;

public interface HttpFrontendManager {

	HttpFrontend createHttpServer(FrontendConfig config, HttpRequestListener listener);
	
	HttpFrontend createHttpsServer(FrontendConfig config, HttpRequestListener listener, SSLEngineFactory factory);

}
