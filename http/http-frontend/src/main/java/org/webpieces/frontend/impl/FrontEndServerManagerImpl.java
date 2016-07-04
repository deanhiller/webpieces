package org.webpieces.frontend.impl;

import java.util.concurrent.ScheduledExecutorService;

import org.webpieces.asyncserver.api.AsyncServer;
import org.webpieces.asyncserver.api.AsyncServerManager;
import org.webpieces.frontend.api.FrontendConfig;
import org.webpieces.frontend.api.HttpFrontend;
import org.webpieces.frontend.api.HttpFrontendManager;
import org.webpieces.frontend.api.HttpRequestListener;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.nio.api.SSLEngineFactory;

public class FrontEndServerManagerImpl implements HttpFrontendManager {

	private AsyncServerManager svrManager;
	private HttpParser parser;
	private ScheduledExecutorService timer;

	public FrontEndServerManagerImpl(AsyncServerManager svrManager, ScheduledExecutorService svc, HttpParser parser) {
		this.timer = svc;
		this.svrManager = svrManager;
		this.parser = parser;
	}

	@Override
	public HttpFrontend createHttpServer(FrontendConfig config, HttpRequestListener listener) {
		preconditionCheck(config);
		
		TimedListener timed = new TimedListener(timer, listener, config);
		HttpFrontendImpl frontend = new HttpFrontendImpl(timed, parser, false);
		AsyncServer tcpServer = svrManager.createTcpServer(config.asyncServerConfig, frontend.getDataListener());
		frontend.init(tcpServer);
		return frontend;
	}

	private void preconditionCheck(FrontendConfig config) {
		if(config.keepAliveTimeoutMs != null && timer == null)
			throw new IllegalArgumentException("keepAliveTimeoutMs must be null since no timer was given when HttpFrontendFactory.createFrontEnd was called");
		else if(config.maxConnectToRequestTimeoutMs != null && timer == null)
			throw new IllegalArgumentException("keepAliveTimeoutMs must be null since no timer was given when HttpFrontendFactory.createFrontEnd was called");
	}

	@Override
	public HttpFrontend createHttpsServer(FrontendConfig config, HttpRequestListener listener,
			SSLEngineFactory factory) {
		preconditionCheck(config);
		TimedListener timed = new TimedListener(timer, listener, config);
		HttpFrontendImpl frontend = new HttpFrontendImpl(timed, parser, true);
		AsyncServer tcpServer = svrManager.createTcpServer(config.asyncServerConfig, frontend.getDataListener(), factory);
		frontend.init(tcpServer);
		return frontend;
	}

}
