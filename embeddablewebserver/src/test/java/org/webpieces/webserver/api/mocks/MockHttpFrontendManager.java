package org.webpieces.webserver.api.mocks;

import java.net.InetSocketAddress;

import org.webpieces.httpproxy.api.HttpFrontend;
import org.webpieces.httpproxy.api.HttpFrontendManager;
import org.webpieces.httpproxy.api.HttpRequestListener;
import org.webpieces.nio.api.SSLEngineFactory;

public class MockHttpFrontendManager implements HttpFrontendManager {

	private HttpRequestListener httpListener;

	@Override
	public HttpFrontend createHttpServer(String id, InetSocketAddress addr, HttpRequestListener listener) {
		this.httpListener = listener;
		return null;
	}

	@Override
	public HttpFrontend createHttpsServer(String id, InetSocketAddress addr, HttpRequestListener listener,
			SSLEngineFactory factory) {
		if(listener != httpListener) 
			throw new IllegalStateException("We expect both http listeners to be the same");
		return null;
	}

	public HttpRequestListener getHttpListener() {
		return httpListener;
	}

}
