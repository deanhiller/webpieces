package org.webpieces.asyncserver.impl;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.DataChunk;
import org.webpieces.nio.api.handlers.DataListener;

public class ProxyDataListener implements DataListener {

	private ConnectedChannels connectedChannels;
	private DataListener dataListener;

	public ProxyDataListener(ConnectedChannels connectedChannels, DataListener dataListener) {
		this.connectedChannels = connectedChannels;
		this.dataListener = dataListener;
	}

	@Override
	public void incomingData(Channel channel, DataChunk b) throws IOException {
		TCPChannel proxy = new ProxyTCPChannel((TCPChannel) channel, connectedChannels);
		dataListener.incomingData(proxy, b);
	}

	@Override
	public void farEndClosed(Channel channel) {
		connectedChannels.removeChannel((TCPChannel) channel);
		TCPChannel proxy = new ProxyTCPChannel((TCPChannel) channel, connectedChannels);
		dataListener.farEndClosed(proxy);
	}

	@Override
	public void failure(Channel channel, ByteBuffer data, Exception e) {
		TCPChannel proxy = new ProxyTCPChannel((TCPChannel) channel, connectedChannels);
		dataListener.failure(proxy, data, e);
	}

}
