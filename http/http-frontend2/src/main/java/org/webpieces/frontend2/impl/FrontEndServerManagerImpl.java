package org.webpieces.frontend2.impl;

import java.util.concurrent.ScheduledExecutorService;

import org.webpieces.asyncserver.api.AsyncServer;
import org.webpieces.asyncserver.api.AsyncServerManager;
import org.webpieces.frontend2.api.HttpFrontendManager;
import org.webpieces.frontend2.api.HttpServer;
import org.webpieces.frontend2.api.HttpSvrConfig;
import org.webpieces.frontend2.api.StreamListener;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.nio.api.SSLEngineFactory;
import org.webpieces.util.futures.FutureHelper;

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
	public HttpServer createHttpServer(HttpSvrConfig config, StreamListener httpListener) {
		preconditionCheck(config);
		ProxyStreamListener proxyStreamListener = new ProxyStreamListener(httpListener);
		Layer1ServerListener listener = buildDatalListener(proxyStreamListener, false);
		AsyncServer tcpServer = svrManager.createTcpServer(config.asyncServerConfig, listener);
		HttpServerImpl frontend = new HttpServerImpl(tcpServer, config, listener);

		return frontend;
	}

	private Layer1ServerListener buildDatalListener(StreamListener httpListener, boolean isBackend) {
		Layer2Http11Handler http11 = new Layer2Http11Handler(httpParser, httpListener);
		Layer2Http2Handler http2 = new Layer2Http2Handler(http2EngineFactory, httpListener);
		FutureHelper futureUtil = new FutureHelper();
		Layer1ServerListener listener = new Layer1ServerListener(futureUtil, http11, http2, isBackend);
		return listener;
	}

	private void preconditionCheck(HttpSvrConfig config) {
		if(config.bindAddress == null)
			throw new IllegalArgumentException("config.bindAddress must be set");
		if(config.keepAliveTimeoutMs != null && timer == null)
			throw new IllegalArgumentException("keepAliveTimeoutMs must be null since no timer was given when HttpFrontendFactory.createFrontEnd was called");
		else if(config.maxConnectToRequestTimeoutMs != null && timer == null)
			throw new IllegalArgumentException("keepAliveTimeoutMs must be null since no timer was given when HttpFrontendFactory.createFrontEnd was called");
	}

	@Override
	public HttpServer createHttpsServer(HttpSvrConfig config, StreamListener httpListener,
                                        SSLEngineFactory factory) {
		preconditionCheck(config);
		ProxyStreamListener proxyStreamListener = new ProxyStreamListener(httpListener);
		Layer1ServerListener listener = buildDatalListener(proxyStreamListener, false);
		AsyncServer tcpServer = svrManager.createTcpServer(config.asyncServerConfig, listener, factory);
		HttpServerImpl frontend = new HttpServerImpl(tcpServer, config, listener);
		
		return frontend;
	}


	@Override
	public HttpServer createUpgradableServer(HttpSvrConfig config, StreamListener httpListener, SSLEngineFactory factory) {
		preconditionCheck(config);
		ProxyStreamListener proxyStreamListener = new ProxyStreamListener(httpListener);
		Layer1ServerListener listener = buildDatalListener(proxyStreamListener, false);
		AsyncServer tcpServer = svrManager.createUpgradableServer(config.asyncServerConfig, listener, factory);
		HttpServerImpl frontend = new HttpServerImpl(tcpServer, config, listener);
		
		return frontend;
	}
	
	@Override
	public HttpServer createBackendHttpsServer(HttpSvrConfig config, StreamListener httpListener, SSLEngineFactory factory) {
		preconditionCheck(config);
		ProxyStreamListener proxyStreamListener = new ProxyStreamListener(httpListener);
		Layer1ServerListener listener = buildDatalListener(proxyStreamListener, true);
		AsyncServer tcpServer = svrManager.createTcpServer(config.asyncServerConfig, listener, factory);
		HttpServerImpl frontend = new HttpServerImpl(tcpServer, config, listener);
		
		return frontend;
	}

}
