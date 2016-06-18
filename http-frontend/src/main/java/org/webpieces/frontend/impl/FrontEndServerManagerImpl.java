package org.webpieces.frontend.impl;

import java.net.InetSocketAddress;

import org.webpieces.asyncserver.api.AsyncServer;
import org.webpieces.asyncserver.api.AsyncServerManager;
import org.webpieces.frontend.api.HttpFrontend;
import org.webpieces.frontend.api.HttpFrontendManager;
import org.webpieces.frontend.api.HttpRequestListener;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.nio.api.SSLEngineFactory;

public class FrontEndServerManagerImpl implements HttpFrontendManager {

	private AsyncServerManager svrManager;
	private HttpParser parser;

	public FrontEndServerManagerImpl(AsyncServerManager svrManager, HttpParser parser) {
		this.svrManager = svrManager;
		this.parser = parser;
	}

	@Override
	public HttpFrontend createHttpServer(String id, InetSocketAddress addr, HttpRequestListener listener) {
		HttpFrontendImpl frontend = new HttpFrontendImpl(listener, parser, false);
		AsyncServer tcpServer = svrManager.createTcpServer(id, addr, frontend.getDataListener());
		frontend.init(tcpServer);
		return frontend;
	}

	@Override
	public HttpFrontend createHttpsServer(String id, InetSocketAddress addr, HttpRequestListener listener,
			SSLEngineFactory factory) {
		HttpFrontendImpl frontend = new HttpFrontendImpl(listener, parser, true);
		AsyncServer tcpServer = svrManager.createTcpServer(id, addr, frontend.getDataListener(), factory);
		frontend.init(tcpServer);
		return frontend;
	}

}
