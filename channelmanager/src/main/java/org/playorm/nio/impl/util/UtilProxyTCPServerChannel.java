package org.playorm.nio.impl.util;

import org.playorm.nio.api.channels.RegisterableChannel;
import org.playorm.nio.api.channels.TCPServerChannel;
import org.playorm.nio.api.handlers.ConnectionListener;


public class UtilProxyTCPServerChannel extends UtilRegisterable implements TCPServerChannel {

	public UtilProxyTCPServerChannel(RegisterableChannel realChannel) {
		super(realChannel);
	}

	protected TCPServerChannel getRealChannel() {
		return (TCPServerChannel)super.getRealChannel();
	}
	
	public void registerServerSocketChannel(ConnectionListener cb) {
		getRealChannel().registerServerSocketChannel(new UtilProxyAcceptCb(this, cb));
	}
	public void oldClose() {
		getRealChannel().oldClose();
	}

}
