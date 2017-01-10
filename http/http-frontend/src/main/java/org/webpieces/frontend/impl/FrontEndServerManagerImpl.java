package org.webpieces.frontend.impl;

import java.util.concurrent.ScheduledExecutorService;

import org.webpieces.asyncserver.api.AsyncServer;
import org.webpieces.asyncserver.api.AsyncServerManager;
import org.webpieces.data.api.BufferPool;
import org.webpieces.frontend.api.FrontendConfig;
import org.webpieces.frontend.api.HttpFrontendManager;
import org.webpieces.frontend.api.HttpServer;
import org.webpieces.httpcommon.api.RequestListener;
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
	public HttpServer createHttpServer(FrontendConfig config, RequestListener listener) {
		preconditionCheck(config);
		
		TimedRequestListener timed = new TimedRequestListener(timer, listener, config);
		HttpServerImpl frontend = new HttpServerImpl(timed, bufferPool, config);
		AsyncServer tcpServer = svrManager.createTcpServer(config.asyncServerConfig, frontend.getDataListener());
		frontend.init(tcpServer);
		return frontend;
	}

	private void preconditionCheck(FrontendConfig config) {
		if(config.bindAddress == null)
			throw new IllegalArgumentException("config.bindAddress cannot be null");
		if(config.keepAliveTimeoutMs != null && timer == null)
			throw new IllegalArgumentException("keepAliveTimeoutMs must be null since no timer was given when HttpFrontendFactory.createFrontEnd was called");
		else if(config.maxConnectToRequestTimeoutMs != null && timer == null)
			throw new IllegalArgumentException("keepAliveTimeoutMs must be null since no timer was given when HttpFrontendFactory.createFrontEnd was called");
	}

	@Override
	public HttpServer createHttpsServer(FrontendConfig config, RequestListener listener,
                                        SSLEngineFactory factory) {
		preconditionCheck(config);
		TimedRequestListener timed = new TimedRequestListener(timer, listener, config);
		HttpServerImpl frontend = new HttpServerImpl(timed, bufferPool, config);
		AsyncServer tcpServer = svrManager.createTcpServer(config.asyncServerConfig, frontend.getDataListener(), factory);
		frontend.init(tcpServer);

		return frontend;
	}

}
