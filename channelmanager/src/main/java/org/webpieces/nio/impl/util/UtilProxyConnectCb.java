package org.webpieces.nio.impl.util;


import java.io.IOException;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.RegisterableChannel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.deprecated.ConnectionCallback;
import org.webpieces.nio.api.handlers.ConnectionListener;


public class UtilProxyConnectCb implements ConnectionCallback {

	//private static final Logger log = Logger.getLogger(ProxyConnectCb.class.getName());
	private TCPChannel channel;
	private ConnectionListener cb;

	public UtilProxyConnectCb(TCPChannel c, ConnectionListener cb) {
		this.channel = c;
		this.cb = cb;
	}

	public void connected(Channel realChannel) throws IOException {
		cb.connected(channel);
	}

	public void failed(RegisterableChannel realChannel, Throwable e) {
		cb.failed(channel, e);
	}

}
