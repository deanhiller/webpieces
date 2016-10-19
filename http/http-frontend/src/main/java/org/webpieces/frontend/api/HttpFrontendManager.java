package org.webpieces.frontend.api;

import org.webpieces.httpcommon.api.RequestListener;
import org.webpieces.nio.api.SSLEngineFactory;

public interface HttpFrontendManager {

	HttpServer createHttpServer(FrontendConfig config, RequestListener listener);
	
	HttpServer createHttpsServer(FrontendConfig config, RequestListener listener, SSLEngineFactory factory);

}
