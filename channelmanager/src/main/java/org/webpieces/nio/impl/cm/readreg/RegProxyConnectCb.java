package org.webpieces.nio.impl.cm.readreg;


import java.io.IOException;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.RegisterableChannel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.deprecated.ConnectionCallback;
import org.webpieces.nio.api.handlers.ConnectionListener;


class RegProxyConnectCb implements ConnectionCallback {

	private TCPChannel regChannel;
	private ConnectionListener cb;

	public RegProxyConnectCb(TCPChannel regChan, ConnectionListener cb) {
		this.regChannel = regChan;
		this.cb = cb;
	}
	
	public void connected(Channel channel) throws IOException {
		cb.connected(regChannel);
	}

	public void failed(RegisterableChannel channel, Throwable e) {
		cb.failed(regChannel, e);
	}
}
