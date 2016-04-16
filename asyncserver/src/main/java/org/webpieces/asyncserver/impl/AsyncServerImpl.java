package org.webpieces.asyncserver.impl;

import java.nio.ByteBuffer;

import org.webpieces.asyncserver.api.AsyncServer;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.channels.TCPServerChannel;

public class AsyncServerImpl implements AsyncServer {

	private TCPServerChannel serverChannel;
	private ConnectedChannels connectedChannels;
	private DefaultConnectionListener connectionListener;
	
	public AsyncServerImpl(TCPServerChannel serverChannel2, DefaultConnectionListener connectionListener,
			ConnectedChannels connectedChannels2) {
		this.serverChannel = serverChannel2;
		this.connectedChannels = connectedChannels2;
		this.connectionListener = connectionListener;
	}

	@Override
	public void enableOverloadMode(ByteBuffer overloadResponse) {
		connectionListener.enableOverloadMode(overloadResponse);
	}

	@Override
	public void disableOverloadMode() {
		connectionListener.disableOverloadMode();
	}

	@Override
	public void closeServerChannel() {
		serverChannel.closeServerChannel();
		
		for(TCPChannel channel : connectedChannels.getAllChannels()) {
			channel.close();
		}
	}

}
