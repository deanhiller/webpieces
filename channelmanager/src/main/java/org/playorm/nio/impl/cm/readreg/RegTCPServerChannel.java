package org.playorm.nio.impl.cm.readreg;

import org.playorm.nio.api.channels.TCPServerChannel;
import org.playorm.nio.api.handlers.ConnectionListener;
import org.playorm.nio.impl.util.UtilRegisterable;


class RegTCPServerChannel extends UtilRegisterable implements TCPServerChannel {

	private TCPServerChannel realChannel;
	
	public RegTCPServerChannel(TCPServerChannel c) {
		super(c);
		realChannel = c;
	}

	public TCPServerChannel getRealChannel() {
		return realChannel;
	}
	
	public void oldClose() {
		realChannel.oldClose();
	}
	
	public void registerServerSocketChannel(ConnectionListener cb) {
		RegProxyAcceptCb proxy = new RegProxyAcceptCb(this, cb);
		realChannel.registerServerSocketChannel(proxy);
	}
}
