package org.webpieces.frontend2.impl;

import java.util.concurrent.Executor;
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

import com.webpieces.http2engine.impl.shared.HeaderSettings;

public class FrontEndServerManagerImpl implements HttpFrontendManager {

	private static final Logger log = LoggerFactory.getLogger(FrontEndServerManagerImpl.class);
	private AsyncServerManager svrManager;
	private BufferPool bufferPool;
	private ScheduledExecutorService timer;
	private ParsingLogic parsing;
	private Executor backupPool;

	public FrontEndServerManagerImpl(AsyncServerManager svrManager, ScheduledExecutorService svc, BufferPool bufferPool, ParsingLogic parsing, Executor backupPool) {
		this.timer = svc;
		this.svrManager = svrManager;
		this.bufferPool = bufferPool;
		this.parsing = parsing;
		this.backupPool = backupPool;
	}

	@Override
	public HttpServer createHttpServer(FrontendConfig config, HttpRequestListener httpListener) {
		preconditionCheck(config);

		ServerListener listener = buildDatalListener(httpListener, config.localSettings);
		AsyncServer tcpServer = svrManager.createTcpServer(config.asyncServerConfig, listener);
		HttpServerImpl frontend = new HttpServerImpl(tcpServer, config);

		return frontend;
	}

	private ServerListener buildDatalListener(HttpRequestListener httpListener, HeaderSettings localSettings) {
		Http1_1Handler http1_1 = new Http1_1Handler(parsing.getHttpParser(), httpListener);
		Http2Handler http2 = new Http2Handler(parsing.getSvrEngineFactory(), parsing.getHttp2Parser(), httpListener, localSettings, backupPool);
		ServerListener listener = new ServerListener(http1_1, http2);
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
		
		ServerListener listener = buildDatalListener(httpListener, config.localSettings);
		AsyncServer tcpServer = svrManager.createTcpServer(config.asyncServerConfig, listener, factory);
		HttpServerImpl frontend = new HttpServerImpl(tcpServer, config);
		
		return frontend;
	}

}
