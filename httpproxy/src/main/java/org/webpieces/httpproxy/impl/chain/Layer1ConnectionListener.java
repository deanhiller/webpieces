package org.webpieces.httpproxy.impl.chain;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.RegisterableChannel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.ConnectionListener;

@Singleton
public class Layer1ConnectionListener implements ConnectionListener {
	private static final Logger log = LoggerFactory.getLogger(Layer1ConnectionListener.class);

	@Inject
	private Layer1DataListener listener;
	
	@Override
	public void connected(Channel channel) throws IOException {
		TCPChannel tcpChannel = (TCPChannel) channel;
		tcpChannel.registerForReads(listener);
	}

	@Override
	public void failed(RegisterableChannel channel, Throwable e) {
		log.warn("exception from client connecting in. channel="+channel, e);
	}

}
