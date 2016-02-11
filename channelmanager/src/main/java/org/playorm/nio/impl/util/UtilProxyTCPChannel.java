package org.playorm.nio.impl.util;

import java.net.SocketAddress;

import org.playorm.nio.api.channels.Channel;
import org.playorm.nio.api.channels.TCPChannel;
import org.playorm.nio.api.deprecated.ConnectionCallback;
import org.playorm.nio.api.handlers.DataListener;


public class UtilProxyTCPChannel extends UtilTCPChannel implements TCPChannel {

	public UtilProxyTCPChannel(Channel realChannel) {
		super(realChannel);
	}

	protected TCPChannel getRealChannel() {
		return (TCPChannel)super.getRealChannel();
	}
	
	public void registerForReads(DataListener listener) {
		getRealChannel().registerForReads(new UtilReaderProxy(this, listener));
	}

	public void oldConnect(SocketAddress addr, ConnectionCallback c) {
		getRealChannel().oldConnect(addr, new UtilProxyConnectCb(this, c));
	}
}
