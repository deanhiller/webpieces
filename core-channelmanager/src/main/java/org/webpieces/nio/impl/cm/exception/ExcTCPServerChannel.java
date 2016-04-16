package org.webpieces.nio.impl.cm.exception;

import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.impl.util.UtilRegisterable;


class ExcTCPServerChannel extends UtilRegisterable implements TCPServerChannel {

	private TCPServerChannel realChannel;
	
	public ExcTCPServerChannel(TCPServerChannel c) {
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
		ExcProxyAcceptCb proxy = new ExcProxyAcceptCb(this, cb);
		realChannel.registerServerSocketChannel(proxy);
	}
}
