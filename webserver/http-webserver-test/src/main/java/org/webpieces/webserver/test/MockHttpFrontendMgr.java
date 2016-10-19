package org.webpieces.webserver.test;

import org.webpieces.frontend.api.FrontendConfig;
import org.webpieces.frontend.api.HttpServer;
import org.webpieces.frontend.api.HttpFrontendManager;
import org.webpieces.httpcommon.api.RequestListener;
import org.webpieces.nio.api.SSLEngineFactory;

public class MockHttpFrontendMgr implements HttpFrontendManager {

	@Override
	public HttpServer createHttpServer(FrontendConfig config, RequestListener listener) {
		return new MockHttpServer();
	}

	@Override
	public HttpServer createHttpsServer(FrontendConfig config, RequestListener listener,
                                        SSLEngineFactory factory) {
		return new MockHttpServer();
	}

}
