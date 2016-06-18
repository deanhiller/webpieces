package org.webpieces.frontend.api;

import java.net.InetSocketAddress;

import org.webpieces.nio.api.SSLEngineFactory;

public interface HttpFrontendManager {

	HttpFrontend createHttpServer(String id, InetSocketAddress addr, HttpRequestListener listener);
	
	HttpFrontend createHttpsServer(String id, InetSocketAddress addr, HttpRequestListener listener, SSLEngineFactory factory);

}
