package org.webpieces.frontend2.impl;

import java.util.concurrent.ScheduledExecutorService;

import org.webpieces.asyncserver.api.AsyncServer;
import org.webpieces.asyncserver.api.AsyncServerManager;
import org.webpieces.data.api.BufferPool;
import org.webpieces.frontend2.api.FrontendConfig;
import org.webpieces.frontend2.api.HttpFrontendManager;
import org.webpieces.frontend2.api.HttpRequestListener;
import org.webpieces.frontend2.api.HttpServer;
import org.webpieces.frontend2.api.ParsingLogic;
import org.webpieces.nio.api.SSLEngineFactory;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.http2engine.api.client.Http2Config;

public class FrontEndServerManagerImpl implements HttpFrontendManager {

	private static final Logger log = LoggerFactory.getLogger(FrontEndServerManagerImpl.class);
	private AsyncServerManager svrManager;
	private BufferPool bufferPool;
	private ScheduledExecutorService timer;
	private ParsingLogic parsing;

	public FrontEndServerManagerImpl(AsyncServerManager svrManager, ScheduledExecutorService svc, BufferPool bufferPool, ParsingLogic parsing) {
		this.timer = svc;
		this.svrManager = svrManager;
		this.bufferPool = bufferPool;
		this.parsing = parsing;
	}

	@Override
	public HttpServer createHttpServer(FrontendConfig config, HttpRequestListener httpListener) {
		preconditionCheck(config);

		Layer1ServerListener listener = buildDatalListener(httpListener, config.http2Config);
		AsyncServer tcpServer = svrManager.createTcpServer(config.asyncServerConfig, listener);
		HttpServerImpl frontend = new HttpServerImpl(tcpServer, config);

		return frontend;
	}

	private Layer1ServerListener buildDatalListener(HttpRequestListener httpListener, Http2Config config) {
		Layer2Http1_1Handler http1_1 = new Layer2Http1_1Handler(parsing.getHttpParser(), httpListener);
		Layer2Http2Handler http2 = new Layer2Http2Handler(parsing.getSvrEngineFactory(), parsing.getHttp2Parser(), httpListener, config);
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
		
		Layer1ServerListener listener = buildDatalListener(httpListener, config.http2Config);
		AsyncServer tcpServer = svrManager.createTcpServer(config.asyncServerConfig, listener, factory);
		HttpServerImpl frontend = new HttpServerImpl(tcpServer, config);
		
		return frontend;
	}

}
