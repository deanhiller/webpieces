package org.webpieces.nio.impl.cm.packet;


import java.io.IOException;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.RegisterableChannel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.libs.PacketProcessor;
import org.webpieces.nio.api.libs.PacketProcessorFactory;


class PacProxyAcceptCb implements ConnectionListener {

	private TCPServerChannel svrChannel;
	private ConnectionListener cb;
	private PacketProcessorFactory factory;

	public PacProxyAcceptCb(TCPServerChannel svrChannel, PacketProcessorFactory proc, ConnectionListener cb) {
		this.svrChannel = svrChannel;
		this.cb = cb;
		this.factory = proc;
	}
	
	public void connected(Channel channel) throws IOException {
		PacketProcessor processor = factory.createPacketProcessor(channel);
		TCPChannel newChannel = new PacTCPChannel((TCPChannel) channel, processor);
		cb.connected(newChannel);		
	}

	public void failed(RegisterableChannel channel, Throwable e) {
		cb.failed(svrChannel, e);
	}
}
