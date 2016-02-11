package org.playorm.nio.impl.cm.exception;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.playorm.nio.api.channels.Channel;
import org.playorm.nio.api.channels.RegisterableChannel;
import org.playorm.nio.api.channels.TCPChannel;
import org.playorm.nio.api.deprecated.ConnectionCallback;
import org.playorm.nio.api.handlers.DataListener;
import org.playorm.nio.api.handlers.OperationCallback;
import org.playorm.nio.impl.util.UtilTCPChannel;


class ExcTCPChannel extends UtilTCPChannel implements TCPChannel {

	private static final Logger log = Logger.getLogger(ExcTCPChannel.class.getName());
//	private BufferHelper helper = ChannelManagerFactory.bufferHelper(null);
	private static final OperationCallback NULL_WRITE_HANDLER = new NullWriteHandler();
	
	public ExcTCPChannel(TCPChannel channel) {
		super(channel);
	}
	
	public void registerForReads(DataListener listener) {
		ExcProxyDataHandler handler = new ExcProxyDataHandler(this, listener);
		TCPChannel realChannel = getRealChannel();
		realChannel.registerForReads(handler);
	}

	@Override
	public int oldWrite(ByteBuffer b) {
		TCPChannel realChannel = getRealChannel();
		return realChannel.oldWrite(b);
	}
	
	public void oldWrite(ByteBuffer b, OperationCallback h) {
		OperationCallback callback;
		if(h == null) {
			callback = NULL_WRITE_HANDLER;
		}else
			callback = h;
		TCPChannel realChannel = getRealChannel();
		realChannel.oldWrite(b, new ExcProxyWriteHandler(this, callback));
	}
	
	public void oldConnect(SocketAddress addr, ConnectionCallback c) {
		if(c == null)
			throw new IllegalArgumentException("ConnectCallback cannot be null");
		
		ExcProxyConnectCb proxy = new ExcProxyConnectCb(this, c);
		TCPChannel realChannel = getRealChannel();
		realChannel.oldConnect(addr, proxy);
	}

	public void oldClose(OperationCallback h) {
		OperationCallback callback;
		if(h == null) {
			callback = NULL_WRITE_HANDLER;
		}else
			callback = h;
		TCPChannel realChannel = getRealChannel();
		realChannel.oldClose(new ExcProxyWriteHandler(this, callback));
	}	
	
	private static class NullWriteHandler implements OperationCallback {

		public void finished(Channel c) throws IOException {
		}

		public void failed(RegisterableChannel c, Throwable e) {
			log.log(Level.WARNING, "Exception trying to write", e);
		}
		
	}

    
}
