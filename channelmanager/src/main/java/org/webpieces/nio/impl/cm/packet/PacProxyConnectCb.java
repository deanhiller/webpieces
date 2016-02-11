package org.webpieces.nio.impl.cm.packet;


import java.io.IOException;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.RegisterableChannel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.deprecated.ConnectionCallback;
import org.webpieces.nio.api.handlers.ConnectionListener;


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
