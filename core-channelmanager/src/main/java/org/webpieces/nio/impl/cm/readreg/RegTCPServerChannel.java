package org.webpieces.nio.impl.cm.readreg;

import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.impl.util.UtilRegisterable;


class RegTCPServerChannel extends UtilRegisterable implements TCPServerChannel {

	private TCPServerChannel realChannel;
	
	public RegTCPServerChannel(TCPServerChannel c) {
		super(c);
		realChannel = c;
	}

	public TCPServerChannel getRealChannel() {
		return realChannel;
	}
	
	public void closeServerChannel() {
		realChannel.closeServerChannel();
	}
	
	public void registerServerSocketChannel(ConnectionListener cb) {
		RegProxyAcceptCb proxy = new RegProxyAcceptCb(this, cb);
		realChannel.registerServerSocketChannel(proxy);
	}
}
