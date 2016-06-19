package org.webpieces.webserver.impl;

import java.net.InetSocketAddress;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.frontend.api.HttpFrontend;
import org.webpieces.frontend.api.HttpFrontendManager;
import org.webpieces.nio.api.SSLEngineFactory;
import org.webpieces.router.api.RoutingService;
import org.webpieces.webserver.api.WebServer;

public class WebServerImpl implements WebServer {

	private static final Logger log = LoggerFactory.getLogger(WebServerImpl.class);
	
	@Inject @Nullable
	private SSLEngineFactory factory;
	@Inject
	private HttpFrontendManager serverMgr;
	@Inject
	private RequestReceiver serverListener;
	@Inject
	private RoutingService routingService;
	
	private HttpFrontend httpServer;
	private HttpFrontend httpsServer;

	@Override
	public void start() {
		log.info("starting server");
		
		routingService.start();
		
		InetSocketAddress addr = new InetSocketAddress(8080);
		httpServer = serverMgr.createHttpServer("httpProxy", addr, serverListener);
		
		InetSocketAddress sslAddr = new InetSocketAddress(8443);
		httpsServer = serverMgr.createHttpsServer("httpsProxy", sslAddr, serverListener, factory);
		log.info("now listening for incoming connections");
	}

	@Override
	public void stop() {
		httpServer.close();
		httpsServer.close();
	}

}
