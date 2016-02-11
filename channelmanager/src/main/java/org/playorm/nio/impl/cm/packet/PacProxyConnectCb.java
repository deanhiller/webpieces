package org.playorm.nio.impl.cm.packet;


import java.io.IOException;

import org.playorm.nio.api.channels.Channel;
import org.playorm.nio.api.channels.RegisterableChannel;
import org.playorm.nio.api.channels.TCPChannel;
import org.playorm.nio.api.deprecated.ConnectionCallback;
import org.playorm.nio.api.handlers.ConnectionListener;


class PacProxyConnectCb implements ConnectionCallback {

	private TCPChannel packetChannel;
	private ConnectionListener cb;

	public PacProxyConnectCb(TCPChannel packetChannel, ConnectionListener cb) {
		this.packetChannel = packetChannel;
		this.cb = cb;
	}
	
	public void connected(Channel channel) throws IOException {
		cb.connected(packetChannel);
	}

	public void failed(RegisterableChannel channel, Throwable e) {
		cb.failed(packetChannel, e);
	}
}
