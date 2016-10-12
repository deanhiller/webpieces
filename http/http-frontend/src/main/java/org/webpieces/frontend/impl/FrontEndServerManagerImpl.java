package org.webpieces.frontend.impl;

import java.util.concurrent.ScheduledExecutorService;

import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import org.webpieces.asyncserver.api.AsyncServer;
import org.webpieces.asyncserver.api.AsyncServerManager;
import org.webpieces.frontend.api.FrontendConfig;
import org.webpieces.frontend.api.HttpFrontend;
import org.webpieces.frontend.api.HttpFrontendManager;
import org.webpieces.httpcommon.api.RequestListener;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.nio.api.SSLEngineFactory;

public class FrontEndServerManagerImpl implements HttpFrontendManager {

	private static final Logger log = LoggerFactory.getLogger(FrontEndServerManagerImpl.class);
	private AsyncServerManager svrManager;
	private HttpParser parser;
	private ScheduledExecutorService timer;

	public FrontEndServerManagerImpl(AsyncServerManager svrManager, ScheduledExecutorService svc, HttpParser parser) {
		this.timer = svc;
		this.svrManager = svrManager;
		this.parser = parser;
	}

	@Override
	public HttpFrontend createHttpServer(FrontendConfig config, RequestListener listener) {
		preconditionCheck(config);
		
		TimedListener timed = new TimedListener(timer, listener, config);
		HttpFrontendImpl frontend = new HttpFrontendImpl(timed, parser, config, false);
		log.info("starting to listen to http port="+config.asyncServerConfig.bindAddr);
		AsyncServer tcpServer = svrManager.createTcpServer(config.asyncServerConfig, frontend.getDataListener());
		frontend.init(tcpServer);
		log.info("now listening for incoming http requests");
		return frontend;
	}

	private void preconditionCheck(FrontendConfig config) {
		if(config.maxConnectToRequestTimeoutMs != null && timer == null)
			throw new IllegalArgumentException("keepAliveTimeoutMs must be null since no timer was given when HttpFrontendFactory.createFrontEnd was called");
	}

	@Override
	public HttpFrontend createHttpsServer(FrontendConfig config, RequestListener listener,
			SSLEngineFactory factory) {
		preconditionCheck(config);
		TimedListener timed = new TimedListener(timer, listener, config);
		HttpFrontendImpl frontend = new HttpFrontendImpl(timed, parser, config, true);
		log.info("starting to listen to https port="+config.asyncServerConfig.bindAddr);
		AsyncServer tcpServer = svrManager.createTcpServer(config.asyncServerConfig, frontend.getDataListener(), factory);
		frontend.init(tcpServer);
		log.info("now listening for incoming https requests");
		return frontend;
	}

}
