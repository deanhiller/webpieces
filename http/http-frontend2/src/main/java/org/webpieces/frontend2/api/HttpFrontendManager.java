package org.webpieces.frontend2.api;

import org.webpieces.nio.api.SSLEngineFactory;

public interface HttpFrontendManager {

	HttpServer createHttpServer(FrontendConfig config, StreamListener listener);
	
	HttpServer createHttpsServer(FrontendConfig config, StreamListener listener, SSLEngineFactory factory);

}
