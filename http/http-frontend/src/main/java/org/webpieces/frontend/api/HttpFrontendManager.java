package org.webpieces.frontend.api;

import org.webpieces.httpcommon.api.RequestListener;
import org.webpieces.nio.api.SSLEngineFactory;

public interface HttpFrontendManager {

	HttpServerSocket createHttpServer(FrontendConfig config, RequestListener listener);
	
	HttpServerSocket createHttpsServer(FrontendConfig config, RequestListener listener, SSLEngineFactory factory);

}
