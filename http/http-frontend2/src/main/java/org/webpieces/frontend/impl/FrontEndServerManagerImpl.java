package org.webpieces.frontend.impl;

import java.util.concurrent.ScheduledExecutorService;

import org.webpieces.asyncserver.api.AsyncServer;
import org.webpieces.asyncserver.api.AsyncServerManager;
import org.webpieces.data.api.BufferPool;
import org.webpieces.frontend.api.FrontendConfig;
import org.webpieces.frontend.api.HttpFrontendManager;
import org.webpieces.frontend.api.HttpRequestListener;
import org.webpieces.frontend.api.HttpServer;
import org.webpieces.nio.api.SSLEngineFactory;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public class FrontEndServerManagerImpl implements HttpFrontendManager {

	private static final Logger log = LoggerFactory.getLogger(FrontEndServerManagerImpl.class);
	private AsyncServerManager svrManager;
	private BufferPool bufferPool;
	private ScheduledExecutorService timer;

	public FrontEndServerManagerImpl(AsyncServerManager svrManager, ScheduledExecutorService svc, BufferPool bufferPool) {
		this.timer = svc;
		this.svrManager = svrManager;
		this.bufferPool = bufferPool;
	}

	@Override
	public HttpServer createHttpServer(FrontendConfig config, HttpRequestListener listener) {
		preconditionCheck(config);

		AsyncServer tcpServer = svrManager.createTcpServer(config.asyncServerConfig, null);

		HttpServerImpl frontend = new HttpServerImpl(listener, bufferPool, config);
		log.info("starting to listen to http port="+config.asyncServerConfig.bindAddr);
		frontend.init(tcpServer);
		log.info("now listening for incoming http requests");
		return frontend;
	}

	private void preconditionCheck(FrontendConfig config) {
		if(config.keepAliveTimeoutMs != null && timer == null)
			throw new IllegalArgumentException("keepAliveTimeoutMs must be null since no timer was given when HttpFrontendFactory.createFrontEnd was called");
		else if(config.maxConnectToRequestTimeoutMs != null && timer == null)
			throw new IllegalArgumentException("keepAliveTimeoutMs must be null since no timer was given when HttpFrontendFactory.createFrontEnd was called");
	}

	@Override
	public HttpServer createHttpsServer(FrontendConfig config, HttpRequestListener listener,
                                        SSLEngineFactory factory) {
		preconditionCheck(config);
		HttpServerImpl frontend = new HttpServerImpl(listener, bufferPool, config);
		log.info("starting to listen to https port="+config.asyncServerConfig.bindAddr);
		AsyncServer tcpServer = svrManager.createTcpServer(config.asyncServerConfig, frontend.getDataListener(), factory);
		frontend.init(tcpServer);
		log.info("now listening for incoming https requests");
		return frontend;
	}

}
