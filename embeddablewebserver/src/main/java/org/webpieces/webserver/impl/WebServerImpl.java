package org.webpieces.webserver.impl;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.asyncserver.api.AsyncConfig;
import org.webpieces.frontend.api.HttpFrontend;
import org.webpieces.frontend.api.HttpFrontendManager;
import org.webpieces.nio.api.SSLEngineFactory;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.router.api.RoutingService;
import org.webpieces.webserver.api.WebServer;
import org.webpieces.webserver.api.WebServerConfig;

public class WebServerImpl implements WebServer {

	private static final Logger log = LoggerFactory.getLogger(WebServerImpl.class);
	
	@Inject
	private WebServerConfig config;
	
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

		AsyncConfig svrChanConfig = new AsyncConfig("http", config.getHttpListenAddress());
		svrChanConfig.functionToConfigureBeforeBind = config.getFunctionToConfigureServerSocket();
		log.info("starting to listen to http port="+svrChanConfig.bindAddr);
		httpServer = serverMgr.createHttpServer(svrChanConfig, serverListener);
		
		if(factory != null) {
			AsyncConfig secureChanConfig = new AsyncConfig("https", config.getHttpsListenAddress());
			secureChanConfig.functionToConfigureBeforeBind = config.getFunctionToConfigureServerSocket();
			log.info("starting to listen to https port="+secureChanConfig.bindAddr);
			httpsServer = serverMgr.createHttpsServer(secureChanConfig, serverListener, factory);
		} else {
			log.info("https port is disabled since configuration had no sslEngineFactory");
		}
		
		log.info("now listening for incoming connections");
	}

	@Override
	public void stop() {
		httpServer.close();
		if(httpsServer != null)
			httpsServer.close();
	}

	@Override
	public TCPServerChannel getUnderlyingHttpChannel() {
		return httpServer.getUnderlyingChannel();
	}
	
	@Override
	public TCPServerChannel getUnderlyingHttpsChannel() {
		return httpsServer.getUnderlyingChannel();
	}

}
