package org.webpieces.frontend.api;

import org.webpieces.httpcommon.api.RequestListener;
import org.webpieces.nio.api.SSLEngineFactory;

public interface HttpFrontendManager {

	HttpFrontend createHttpServer(FrontendConfig config, RequestListener listener);
	
	HttpFrontend createHttpsServer(FrontendConfig config, RequestListener listener, SSLEngineFactory factory);

}
