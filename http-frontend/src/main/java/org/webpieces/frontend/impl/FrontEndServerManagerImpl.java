package org.webpieces.frontend.impl;

import org.webpieces.asyncserver.api.AsyncConfig;
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
	public HttpFrontend createHttpServer(AsyncConfig config, HttpRequestListener listener) {
		HttpFrontendImpl frontend = new HttpFrontendImpl(listener, parser, false);
		AsyncServer tcpServer = svrManager.createTcpServer(config, frontend.getDataListener());
		frontend.init(tcpServer);
		return frontend;
	}

	@Override
	public HttpFrontend createHttpsServer(AsyncConfig config, HttpRequestListener listener,
			SSLEngineFactory factory) {
		HttpFrontendImpl frontend = new HttpFrontendImpl(listener, parser, true);
		AsyncServer tcpServer = svrManager.createTcpServer(config, frontend.getDataListener(), factory);
		frontend.init(tcpServer);
		return frontend;
	}

}
