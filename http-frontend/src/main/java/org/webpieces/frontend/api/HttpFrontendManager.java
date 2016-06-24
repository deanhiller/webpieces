package org.webpieces.frontend.api;

import org.webpieces.asyncserver.api.AsyncConfig;
import org.webpieces.nio.api.SSLEngineFactory;

public interface HttpFrontendManager {

	HttpFrontend createHttpServer(AsyncConfig config, HttpRequestListener listener);
	
	HttpFrontend createHttpsServer(AsyncConfig config, HttpRequestListener listener, SSLEngineFactory factory);

}
