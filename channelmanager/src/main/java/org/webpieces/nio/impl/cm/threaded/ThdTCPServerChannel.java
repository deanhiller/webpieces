package org.webpieces.nio.impl.cm.threaded;

import java.util.concurrent.Executor;

import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.libs.BufferFactory;
import org.webpieces.nio.impl.util.UtilRegisterable;


class ThdTCPServerChannel extends UtilRegisterable implements TCPServerChannel {

	private TCPServerChannel realChannel;
	private Executor svc;
	private BufferFactory bufFactory;
	
	public ThdTCPServerChannel(TCPServerChannel c, Executor svc, BufferFactory bufFactory) {
		super(c);
		realChannel = c;
		this.svc = svc;
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
