package org.webpieces.httpproxy.impl;

import java.net.InetSocketAddress;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.asyncserver.api.AsyncConfig;
import org.webpieces.frontend.api.HttpFrontend;
import org.webpieces.frontend.api.HttpFrontendManager;
import org.webpieces.httpproxy.api.HttpProxy;
import org.webpieces.httpproxy.impl.chain.Layer4Processor;

public class HttpProxyImpl implements HttpProxy {

	private static final Logger log = LoggerFactory.getLogger(HttpProxyImpl.class);
	
	@Inject
	private HttpFrontendManager serverMgr;
	@Inject
	private Layer4Processor serverListener;
	
	private HttpFrontend httpServer;
	
	@Override
	public void start() {
		log.info("starting server");
		InetSocketAddress addr = new InetSocketAddress(8080);
		AsyncConfig config = new AsyncConfig("httpProxy", addr);
		config.functionToConfigureBeforeBind = s -> s.socket().setReuseAddress(true);
		httpServer = serverMgr.createHttpServer(config, serverListener);
		
//		InetSocketAddress sslAddr = new InetSocketAddress(8443);
//		httpsServer = serverMgr.createTcpServer("httpsProxy", sslAddr, sslServerListener);
		log.info("now listening for incoming connections");
	}

	@Override
	public void stop() {
		httpServer.close();
	}

}
