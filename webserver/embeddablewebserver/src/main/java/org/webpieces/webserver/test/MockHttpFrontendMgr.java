package org.webpieces.webserver.test;

import org.webpieces.frontend.api.FrontendConfig;
import org.webpieces.frontend.api.HttpFrontend;
import org.webpieces.frontend.api.HttpFrontendManager;
import org.webpieces.frontend.api.HttpRequestListener;
import org.webpieces.nio.api.SSLEngineFactory;

public class MockHttpFrontendMgr implements HttpFrontendManager {

	@Override
	public HttpFrontend createHttpServer(FrontendConfig config, HttpRequestListener listener) {
		return new MockHttpFrontend();
	}

	@Override
	public HttpFrontend createHttpsServer(FrontendConfig config, HttpRequestListener listener,
			SSLEngineFactory factory) {
		return new MockHttpFrontend();
	}

}
