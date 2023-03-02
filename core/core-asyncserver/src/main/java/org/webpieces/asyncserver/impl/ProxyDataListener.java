package org.webpieces.asyncserver.impl;

import java.nio.ByteBuffer;
import org.webpieces.util.futures.XFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.asyncserver.api.AsyncDataListener;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.DataListener;

public class ProxyDataListener implements DataListener {

	private static final Logger log = LoggerFactory.getLogger(ProxyDataListener.class);
	private ConnectedChannels connectedChannels;
	private AsyncDataListener dataListener;

	public ProxyDataListener(ConnectedChannels connectedChannels, AsyncDataListener dataListener) {
		this.connectedChannels = connectedChannels;
		this.dataListener = dataListener;
	}

	@Override
	public XFuture<Void> incomingData(Channel channel, ByteBuffer b) {
		return dataListener.incomingData(channel, b);
	}

	@Override
	public void farEndClosed(Channel channel) {
		if(log.isDebugEnabled())
			log.debug(channel+"far end closed");
		dataListener.farEndClosed(channel);
		connectedChannels.removeChannel((TCPChannel) channel);
	}

	@Override
	public void failure(Channel channel, ByteBuffer data, Exception e) {
		dataListener.failure(channel, data, e);
	}

	public void connectionOpened(Channel channel, boolean isReadyForWrites) {
		if(log.isDebugEnabled())
			log.debug("connection opened");
		dataListener.connectionOpened((TCPChannel) channel, isReadyForWrites);
	}

}
