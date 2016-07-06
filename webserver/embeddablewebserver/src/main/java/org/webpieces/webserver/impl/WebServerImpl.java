package org.webpieces.webserver.impl;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.frontend.api.FrontendConfig;
import org.webpieces.frontend.api.HttpFrontend;
import org.webpieces.frontend.api.HttpFrontendManager;
import org.webpieces.frontend.api.HttpRequestListener;
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
	public HttpRequestListener start() {
		log.info("starting server");
		
		routingService.start();

		FrontendConfig svrChanConfig = new FrontendConfig("http", config.getHttpListenAddress());
		svrChanConfig.asyncServerConfig.functionToConfigureBeforeBind = config.getFunctionToConfigureServerSocket();
		httpServer = serverMgr.createHttpServer(svrChanConfig, serverListener);
		
		if(factory != null) {
			FrontendConfig secureChanConfig = new FrontendConfig("https", config.getHttpsListenAddress());
			secureChanConfig.asyncServerConfig.functionToConfigureBeforeBind = config.getFunctionToConfigureServerSocket();
			httpsServer = serverMgr.createHttpsServer(secureChanConfig, serverListener, factory);
		} else {
			log.info("https port is disabled since configuration had no sslEngineFactory");
		}
		
		log.info("server started");
		return serverListener;
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
