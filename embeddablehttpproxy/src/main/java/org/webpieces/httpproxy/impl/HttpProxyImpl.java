package org.webpieces.httpproxy.impl;

import java.net.InetSocketAddress;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.asyncserver.api.AsyncServer;
import org.webpieces.asyncserver.api.AsyncServerManager;
import org.webpieces.httpproxy.api.HttpProxy;
import org.webpieces.httpproxy.impl.chain.Layer1DataListener;
import org.webpieces.httpproxy.impl.chain.SSLLayer1DataListener;

public class HttpProxyImpl implements HttpProxy {

	private static final Logger log = LoggerFactory.getLogger(HttpProxyImpl.class);
	
	@Inject
	private AsyncServerManager channelManager;
	@Inject
	private Layer1DataListener serverListener;
	@Inject
	private SSLLayer1DataListener sslServerListener;
	
	private AsyncServer serverChannel;
	private AsyncServer sslServerChannel;
	
	@Override
	public void start() {
		log.info("starting server");
		InetSocketAddress addr = new InetSocketAddress(8080);
		serverChannel = channelManager.createTcpServer("httpProxy", addr, serverListener);

		InetSocketAddress sslAddr = new InetSocketAddress(8443);
		sslServerChannel = channelManager.createTcpServer("httpsProxy", sslAddr, sslServerListener);
		log.info("now listening for incoming connections");
	}

	@Override
	public void stop() {
		serverChannel.closeServerChannel();
		sslServerChannel.closeServerChannel();
	}

}
