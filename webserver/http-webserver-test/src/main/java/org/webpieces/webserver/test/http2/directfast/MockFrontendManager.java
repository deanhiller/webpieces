package org.webpieces.webserver.test.http2.directfast;

import org.webpieces.frontend2.api.HttpFrontendManager;
import org.webpieces.frontend2.api.HttpServer;
import org.webpieces.frontend2.api.HttpSvrConfig;
import org.webpieces.frontend2.api.StreamListener;
import org.webpieces.nio.api.SSLEngineFactory;

public class MockFrontendManager implements HttpFrontendManager {

	private StreamListener httpListener;
	private StreamListener httpsListener;

	@Override
	public HttpServer createHttpServer(HttpSvrConfig config, StreamListener listener) {
		if(httpListener != null)
			throw new IllegalStateException("Somehow another http server is starting yet we are only designed to support a single one right now");
		httpListener = listener;
		return new MockHttpServer(config);
	}

	@Override
	public HttpServer createHttpsServer(HttpSvrConfig config, StreamListener listener, SSLEngineFactory factory) {
		if(httpsListener != null)
			throw new IllegalStateException("Somehow another https server is starting yet we are only designed to support a single one right now");
		httpsListener = listener;
		return new MockHttpServer(config);
	}

	@Override
	public HttpServer createUpgradableServer(HttpSvrConfig config, StreamListener listener, SSLEngineFactory factory) {
		throw new UnsupportedOperationException();
	}

	@Override
	public HttpServer createBackendHttpsServer(HttpSvrConfig config, StreamListener listener,
			SSLEngineFactory factory) {
		throw new UnsupportedOperationException();
	}

	public StreamListener getHttpListener() {
		return httpListener;
	}
	
	public StreamListener getHttpsListener() {
		return httpsListener;
	}

}
