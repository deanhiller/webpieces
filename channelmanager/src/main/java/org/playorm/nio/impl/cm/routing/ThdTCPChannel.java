package org.playorm.nio.impl.cm.routing;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

import org.playorm.nio.api.channels.TCPChannel;
import org.playorm.nio.api.deprecated.ConnectionCallback;
import org.playorm.nio.api.handlers.DataListener;
import org.playorm.nio.api.handlers.OperationCallback;
import org.playorm.nio.api.libs.BufferFactory;
import org.playorm.nio.impl.util.UtilTCPChannel;


class ThdTCPChannel extends UtilTCPChannel implements TCPChannel {

//	private static final Logger log = Logger.getLogger(TCPChannelImpl.class.getName());
//	private BufferHelper helper = ChannelManagerFactory.bufferHelper(null);
	
	private SpecialRoutingExecutor svc;
	private BufferFactory bufFactory;
	
	public ThdTCPChannel(TCPChannel channel, SpecialRoutingExecutor svc2, BufferFactory bufFactory) {
		super(channel);
		this.svc = svc2;
		this.bufFactory = bufFactory;
	}
	
	public void registerForReads(DataListener listener) {
		ThdProxyDataHandler handler = new ThdProxyDataHandler(this, listener, svc, bufFactory);
		TCPChannel realChannel = getRealChannel();
		realChannel.registerForReads(handler);
	}

	@Override
	public int oldWrite(ByteBuffer b) {
		TCPChannel realChannel = getRealChannel();
		return realChannel.oldWrite(b);
	}
	
	public void oldWrite(ByteBuffer b, OperationCallback h) {
		TCPChannel realChannel = getRealChannel();
		realChannel.oldWrite(b, new ThdProxyWriteHandler(this, h, svc));
	}
	
	public void oldConnect(SocketAddress addr, ConnectionCallback c) {
		if(c == null)
			throw new IllegalArgumentException("ConnectCallback cannot be null");
		
		ThdProxyConnectCb proxy = new ThdProxyConnectCb(this, c, svc);
		TCPChannel realChannel = getRealChannel();
		realChannel.oldConnect(addr, proxy);
	}

	public void oldClose(OperationCallback h) {
		TCPChannel realChannel = getRealChannel();
		realChannel.oldClose(new ThdProxyWriteHandler(this, h, svc));
	}    
}
