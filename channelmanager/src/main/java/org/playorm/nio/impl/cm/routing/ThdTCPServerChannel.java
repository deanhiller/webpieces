package org.playorm.nio.impl.cm.routing;

import org.playorm.nio.api.channels.TCPServerChannel;
import org.playorm.nio.api.handlers.ConnectionListener;
import org.playorm.nio.api.libs.BufferFactory;
import org.playorm.nio.impl.util.UtilRegisterable;


class ThdTCPServerChannel extends UtilRegisterable implements TCPServerChannel {

	private TCPServerChannel realChannel;
	private SpecialRoutingExecutor svc;
	private BufferFactory bufFactory;
	
	public ThdTCPServerChannel(TCPServerChannel c, SpecialRoutingExecutor svc2, BufferFactory bufFactory) {
		super(c);
		realChannel = c;
		this.svc = svc2;
		this.bufFactory = bufFactory;
	}

	public TCPServerChannel getRealChannel() {
		return realChannel;
	}
	
	public void oldClose() {
		realChannel.oldClose();
	}
	
	public void registerServerSocketChannel(ConnectionListener cb) {
		ThdProxyAcceptCb proxy = new ThdProxyAcceptCb(this, cb, svc, bufFactory);
		realChannel.registerServerSocketChannel(proxy);
	}
}
