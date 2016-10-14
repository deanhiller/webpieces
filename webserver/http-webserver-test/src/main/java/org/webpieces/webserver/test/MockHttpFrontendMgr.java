package org.webpieces.webserver.test;

import org.webpieces.frontend.api.FrontendConfig;
import org.webpieces.frontend.api.HttpServerSocket;
import org.webpieces.frontend.api.HttpFrontendManager;
import org.webpieces.httpcommon.api.RequestListener;
import org.webpieces.nio.api.SSLEngineFactory;

public class MockHttpFrontendMgr implements HttpFrontendManager {

	@Override
	public HttpServerSocket createHttpServer(FrontendConfig config, RequestListener listener) {
		return new MockHttpServerSocket();
	}

	@Override
	public HttpServerSocket createHttpsServer(FrontendConfig config, RequestListener listener,
                                              SSLEngineFactory factory) {
		return new MockHttpServerSocket();
	}

}
