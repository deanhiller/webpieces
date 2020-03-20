package org.webpieces.asyncserver.impl;

import org.webpieces.asyncserver.api.AsyncConfig;
import org.webpieces.asyncserver.api.AsyncDataListener;
import org.webpieces.asyncserver.api.AsyncServer;
import org.webpieces.asyncserver.api.AsyncServerManager;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.SSLEngineFactory;
import org.webpieces.nio.api.channels.TCPServerChannel;

public class AsyncServerManagerImpl implements AsyncServerManager {

	private ChannelManager channelManager;

	public AsyncServerManagerImpl(ChannelManager channelManager) {
		this.channelManager = channelManager;
	}

	@Override
	public AsyncServer createTcpServer(
			AsyncConfig config, AsyncDataListener listener, SSLEngineFactory sslFactory) {
		return createTcpServerImpl(config, listener, sslFactory);
	}
	
	private AsyncServer createTcpServerImpl(AsyncConfig config,
			AsyncDataListener listener, SSLEngineFactory sslFactory) {
		if(config.id == null)
			throw new IllegalArgumentException("config.id must not be null");
		
		String id = channelManager.getName()+"."+config.id;
		ConnectedChannels connectedChannels = new ConnectedChannels(id);
		ProxyDataListener proxyListener = new ProxyDataListener(connectedChannels, listener);
		DefaultConnectionListener connectionListener = new DefaultConnectionListener(id, connectedChannels, proxyListener); 

		TCPServerChannel serverChannel;
		if(sslFactory != null) {
			serverChannel = channelManager.createTCPServerChannel(config.id, connectionListener, sslFactory);
		} else {
			serverChannel = channelManager.createTCPServerChannel(config.id, connectionListener);
		}

		//MUST be called before bind...
		serverChannel.setReuseAddress(true);
		
		serverChannel.configure(config.functionToConfigureBeforeBind);
		
		return new AsyncServerImpl(serverChannel, connectionListener, proxyListener, sslFactory);
	}

	@Override
	public AsyncServer createTcpServer(AsyncConfig config, AsyncDataListener listener) {
		return createTcpServerImpl(config, listener, null);
	}

	@Override
	public String getName() {
		return channelManager.getName();
	}
}
