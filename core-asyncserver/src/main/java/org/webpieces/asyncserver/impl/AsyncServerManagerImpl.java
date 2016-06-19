package org.webpieces.asyncserver.impl;

import java.net.SocketAddress;

import org.webpieces.asyncserver.api.AsyncServer;
import org.webpieces.asyncserver.api.AsyncServerManager;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.SSLEngineFactory;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.nio.api.handlers.DataListener;

public class AsyncServerManagerImpl implements AsyncServerManager {

	private ChannelManager channelManager;

	public AsyncServerManagerImpl(ChannelManager channelManager) {
		this.channelManager = channelManager;
	}

	@Override
	public AsyncServer createTcpServer(
			String id, SocketAddress addr, DataListener listener, SSLEngineFactory sslFactory) {
		if(sslFactory == null)
			throw new IllegalArgumentException("SSLEngineFactory is null but must be supplied");
		return createTcpServerImpl(id, addr, listener, sslFactory);
	}
	
	private AsyncServer createTcpServerImpl(
			String id, SocketAddress addr, DataListener listener, SSLEngineFactory sslFactory) {
		ConnectedChannels connectedChannels = new ConnectedChannels();
		ProxyDataListener proxyListener = new ProxyDataListener(connectedChannels, listener);
		DefaultConnectionListener connectionListener = new DefaultConnectionListener(connectedChannels, proxyListener); 

		TCPServerChannel serverChannel;
		if(sslFactory != null)
			serverChannel = channelManager.createTCPServerChannel(id, connectionListener, sslFactory);
		else
			serverChannel = channelManager.createTCPServerChannel(id, connectionListener);
		
		serverChannel.bind(addr);
		serverChannel.setReuseAddress(true);
		
		return new AsyncServerImpl(serverChannel, connectionListener, proxyListener);
	}

	@Override
	public AsyncServer createTcpServer(String id, SocketAddress addr, DataListener listener) {
		return createTcpServerImpl(id, addr, listener, null);
	}
}
