package org.playorm.nio.impl.cm.exception;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.playorm.nio.api.channels.Channel;
import org.playorm.nio.api.channels.RegisterableChannel;
import org.playorm.nio.api.channels.TCPChannel;
import org.playorm.nio.api.deprecated.ConnectionCallback;
import org.playorm.nio.api.handlers.ConnectionListener;


class ExcProxyConnectCb implements ConnectionCallback {

	private static final Logger log = Logger.getLogger(ExcProxyConnectCb.class.getName());
	
	private TCPChannel proxyChannel;
	private ConnectionListener cb;

	public ExcProxyConnectCb(TCPChannel channel, ConnectionListener cb) {
		this.proxyChannel = channel;
		this.cb = cb;
	}
	
	public void connected(Channel channel) throws IOException {
		try {
			cb.connected(proxyChannel);
		} catch(Exception e) {
			log.log(Level.WARNING, channel+"Exception", e);
		}
	}

	public void failed(RegisterableChannel channel, Throwable e) {
		try {
			cb.failed(proxyChannel, e);
		} catch(Exception ee) {
			log.log(Level.WARNING, channel+"Exception", ee);
		}
	}
}
