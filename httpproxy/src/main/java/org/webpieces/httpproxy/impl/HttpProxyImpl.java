package org.webpieces.httpproxy.impl;

import java.net.InetSocketAddress;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.httpproxy.api.HttpProxy;
import org.webpieces.httpproxy.impl.chain.Layer1ConnectionListener;
import org.webpieces.httpproxy.impl.chain.SSLLayer1ConnectionListener;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.channels.TCPServerChannel;

public class HttpProxyImpl implements HttpProxy {

	private static final Logger log = LoggerFactory.getLogger(HttpProxyImpl.class);
	
	@Inject
	private ChannelManager channelManager;
	@Inject
	private Layer1ConnectionListener serverListener;
	@Inject
	private SSLLayer1ConnectionListener sslServerListener;
	
	private TCPServerChannel serverChannel;
	private TCPServerChannel sslServerChannel;
	
	@Override
	public void start() {
		log.info("starting server");
		InetSocketAddress addr = new InetSocketAddress(8080);
		serverChannel = channelManager.createTCPServerChannel("httpProxy", addr, serverListener);

		InetSocketAddress sslAddr = new InetSocketAddress(8443);
		sslServerChannel = channelManager.createTCPServerChannel("httpsProxy", sslAddr, sslServerListener);
		log.info("now listening for incoming connections");
	}

	@Override
	public void stop() {
		serverChannel.closeServerChannel();
		sslServerChannel.closeServerChannel();
		
		throw new UnsupportedOperationException("close all tcp channels too");
	}

}
