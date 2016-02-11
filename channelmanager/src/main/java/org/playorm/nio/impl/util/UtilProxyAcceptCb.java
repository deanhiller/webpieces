package org.playorm.nio.impl.util;


import java.io.IOException;

import org.playorm.nio.api.channels.Channel;
import org.playorm.nio.api.channels.RegisterableChannel;
import org.playorm.nio.api.channels.TCPServerChannel;
import org.playorm.nio.api.handlers.ConnectionListener;


public class UtilProxyAcceptCb implements ConnectionListener {

	private TCPServerChannel channel;
	private ConnectionListener cb;

	public UtilProxyAcceptCb(TCPServerChannel c, ConnectionListener cb) {
		this.channel = c;
		this.cb = cb;
	}

	public void connected(Channel realChannel) throws IOException {
		UtilProxyTCPChannel newOne = new UtilProxyTCPChannel(realChannel);
		cb.connected(newOne);
	}

	public void failed(RegisterableChannel realChannel, Throwable e) {
		cb.failed(channel, e);
	}

}
