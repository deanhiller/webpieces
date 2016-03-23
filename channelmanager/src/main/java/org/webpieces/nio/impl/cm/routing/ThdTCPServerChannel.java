package org.webpieces.nio.impl.cm.routing;

import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.libs.BufferFactory;
import org.webpieces.nio.impl.util.UtilRegisterable;


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
	
	public void closeServerChannel() {
		realChannel.closeServerChannel();
	}
	
	public void registerServerSocketChannel(ConnectionListener cb) {
		ThdProxyAcceptCb proxy = new ThdProxyAcceptCb(this, cb, svc, bufFactory);
		realChannel.registerServerSocketChannel(proxy);
	}
}
