package org.webpieces.asyncserver.impl;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.RegisterableChannel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DataListener;

public class DefaultConnectionListener implements ConnectionListener {

	private static final Logger log = LoggerFactory.getLogger(DefaultConnectionListener.class);
	private DataListener dataListener;
	private ConnectedChannels connectedChannels;

	public DefaultConnectionListener(DataListener listener, ConnectedChannels channels) {
		this.dataListener = listener;
		this.connectedChannels = channels;
	}

	@Override
	public void connected(Channel channel) throws IOException {
		TCPChannel tcpChannel = (TCPChannel) channel;
		connectedChannels.addChannel(tcpChannel);
		
		tcpChannel.registerForReads(new ProxyDataListener(connectedChannels, dataListener));
	}

	@Override
	public void failed(RegisterableChannel channel, Throwable e) {
		log.warn("exception from client connecting in. channel="+channel, e);
	}

}
