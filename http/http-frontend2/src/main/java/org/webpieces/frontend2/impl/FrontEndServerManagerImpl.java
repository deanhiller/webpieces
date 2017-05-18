package org.webpieces.frontend2.impl;

import java.util.concurrent.ScheduledExecutorService;

import org.webpieces.asyncserver.api.AsyncServer;
import org.webpieces.asyncserver.api.AsyncServerManager;
import org.webpieces.frontend2.api.FrontendConfig;
import org.webpieces.frontend2.api.HttpFrontendManager;
import org.webpieces.frontend2.api.HttpRequestListener;
import org.webpieces.frontend2.api.HttpServer;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.nio.api.SSLEngineFactory;

import com.webpieces.http2engine.api.server.Http2ServerEngineFactory;

public class FrontEndServerManagerImpl implements HttpFrontendManager {

	private AsyncServerManager svrManager;
	private ScheduledExecutorService timer;
	private HttpParser httpParser;
	private Http2ServerEngineFactory http2EngineFactory;

	public FrontEndServerManagerImpl(
			AsyncServerManager svrManager, ScheduledExecutorService svc, Http2ServerEngineFactory http2EngineFactory, HttpParser httpParser) {
		this.timer = svc;
		this.svrManager = svrManager;
		this.http2EngineFactory = http2EngineFactory;
		this.httpParser = httpParser;
	}

	@Override
	public HttpServer createHttpServer(FrontendConfig config, HttpRequestListener httpListener) {
		preconditionCheck(config);

		Layer1ServerListener listener = buildDatalListener(httpListener, false);
		AsyncServer tcpServer = svrManager.createTcpServer(config.asyncServerConfig, listener);
		HttpServerImpl frontend = new HttpServerImpl(tcpServer, config, listener);

		return frontend;
	}

	private Layer1ServerListener buildDatalListener(HttpRequestListener httpListener, boolean isHttps) {
		Layer2Http1_1Handler http1_1 = new Layer2Http1_1Handler(httpParser, httpListener, isHttps);
		Layer2Http2Handler http2 = new Layer2Http2Handler(http2EngineFactory, httpListener, isHttps);
		Layer1ServerListener listener = new Layer1ServerListener(http1_1, http2);
		return listener;
	}

	private void preconditionCheck(FrontendConfig config) {
		if(config.bindAddress == null)
			throw new IllegalArgumentException("config.bindAddress must be set");
		if(config.keepAliveTimeoutMs != null && timer == null)
			throw new IllegalArgumentException("keepAliveTimeoutMs must be null since no timer was given when HttpFrontendFactory.createFrontEnd was called");
		else if(config.maxConnectToRequestTimeoutMs != null && timer == null)
			throw new IllegalArgumentException("keepAliveTimeoutMs must be null since no timer was given when HttpFrontendFactory.createFrontEnd was called");
	}

	@Override
	public HttpServer createHttpsServer(FrontendConfig config, HttpRequestListener httpListener,
                                        SSLEngineFactory factory) {
		preconditionCheck(config);
		
		Layer1ServerListener listener = buildDatalListener(httpListener, true);
		AsyncServer tcpServer = svrManager.createTcpServer(config.asyncServerConfig, listener, factory);
		HttpServerImpl frontend = new HttpServerImpl(tcpServer, config, listener);
		
		return frontend;
	}

}
