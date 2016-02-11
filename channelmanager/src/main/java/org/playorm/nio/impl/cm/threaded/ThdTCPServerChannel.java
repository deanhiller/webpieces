package org.playorm.nio.impl.cm.threaded;

import java.util.concurrent.Executor;

import org.playorm.nio.api.channels.TCPServerChannel;
import org.playorm.nio.api.handlers.ConnectionListener;
import org.playorm.nio.api.libs.BufferFactory;
import org.playorm.nio.impl.util.UtilRegisterable;


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
	
	public void oldClose() {
		realChannel.oldClose();
	}
	
	public void registerServerSocketChannel(ConnectionListener cb) {
		ThdProxyAcceptCb proxy = new ThdProxyAcceptCb(this, cb, svc, bufFactory);
		realChannel.registerServerSocketChannel(proxy);
	}
}
