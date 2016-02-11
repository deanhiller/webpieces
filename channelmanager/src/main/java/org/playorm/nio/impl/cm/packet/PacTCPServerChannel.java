package org.playorm.nio.impl.cm.packet;

import org.playorm.nio.api.channels.TCPServerChannel;
import org.playorm.nio.api.handlers.ConnectionListener;
import org.playorm.nio.api.libs.PacketProcessorFactory;
import org.playorm.nio.impl.util.UtilRegisterable;


class PacTCPServerChannel extends UtilRegisterable implements TCPServerChannel {

	private TCPServerChannel realChannel;
	private PacketProcessorFactory factory;
	
	public PacTCPServerChannel(TCPServerChannel c, PacketProcessorFactory f) {
		super(c);
		realChannel = c;
		this.factory = f;
	}

	public TCPServerChannel getRealChannel() {
		return realChannel;
	}
	
	public void oldClose() {
		realChannel.oldClose();
	}
	
	public void registerServerSocketChannel(ConnectionListener cb) {
		PacProxyAcceptCb proxy = new PacProxyAcceptCb(this, factory, cb);
		realChannel.registerServerSocketChannel(proxy);
	}
}
