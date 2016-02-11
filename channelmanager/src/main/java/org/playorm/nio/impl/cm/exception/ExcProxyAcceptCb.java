package org.playorm.nio.impl.cm.exception;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.playorm.nio.api.channels.Channel;
import org.playorm.nio.api.channels.RegisterableChannel;
import org.playorm.nio.api.channels.TCPChannel;
import org.playorm.nio.api.channels.TCPServerChannel;
import org.playorm.nio.api.handlers.ConnectionListener;


class ExcProxyAcceptCb implements ConnectionListener {

	private static final Logger log = Logger.getLogger(ExcProxyAcceptCb.class.getName());
	
	private TCPServerChannel svrChannel;
	private ConnectionListener cb;

	public ExcProxyAcceptCb(TCPServerChannel svrChannel, ConnectionListener cb) {
		this.svrChannel = svrChannel;
		this.cb = cb;
	}
	
	public void connected(Channel channel) throws IOException {
		try {
			TCPChannel newChannel = new ExcTCPChannel((TCPChannel) channel);
			cb.connected(newChannel);
		} catch(Exception e) {
			log.log(Level.WARNING, channel+"Exception", e);
		}
	}

	public void failed(RegisterableChannel channel, Throwable e) {
		try {
			cb.failed(svrChannel, e);
		} catch(Exception ee) {
			log.log(Level.WARNING, channel+"Exception", ee);
		}
	}
}
