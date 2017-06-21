package org.webpieces.frontend2.api;

import org.webpieces.nio.api.SSLEngineFactory;

public interface HttpFrontendManager {

	HttpServer createHttpServer(HttpSvrConfig config, StreamListener listener);
	
	HttpServer createHttpsServer(HttpSvrConfig config, StreamListener listener, SSLEngineFactory factory);

}
