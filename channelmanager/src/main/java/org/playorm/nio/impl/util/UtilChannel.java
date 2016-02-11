package org.playorm.nio.impl.util;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

import org.playorm.nio.api.channels.Channel;
import org.playorm.nio.api.handlers.DataListener;
import org.playorm.nio.api.handlers.FutureOperation;
import org.playorm.nio.api.handlers.NullWriteCallback;
import org.playorm.nio.api.handlers.OperationCallback;
import org.playorm.nio.api.libs.ChannelSession;


public abstract class UtilChannel extends UtilRegisterable implements Channel {
    
	public UtilChannel(Channel realChannel) {
		super(realChannel);
	}

	protected Channel getRealChannel() {
		return (Channel)super.getRealChannel();
	}
	
	public FutureOperation write(ByteBuffer b) {
		Channel realChannel = getRealChannel();
		return realChannel.write(b);
	}
	
	public int oldWrite(ByteBuffer b) {
		Channel realChannel = getRealChannel();
		return realChannel.oldWrite(b);
	}
	
	public void oldWrite(ByteBuffer b, OperationCallback h) {
		Channel realChannel = getRealChannel();
		if(h == null)
			realChannel.oldWrite(b, NullWriteCallback.singleton());
		else
			realChannel.oldWrite(b, new UtilPassThroughWriteHandler(this, h));
	}
	
	public void registerForReads(DataListener listener) {
		Channel realChannel = getRealChannel();
		UtilReaderProxy proxy = new UtilReaderProxy(realChannel, listener);
		realChannel.registerForReads(proxy);
	}
	
	public void unregisterForReads() {
		Channel realChannel = getRealChannel();
		realChannel.unregisterForReads();
	}

	@Override
	public FutureOperation connect(SocketAddress addr) {
		Channel realChannel = getRealChannel();
		return realChannel.connect(addr);
	}
	
	public void oldConnect(SocketAddress addr) {    
		Channel realChannel = getRealChannel();
		realChannel.oldConnect(addr);
	}

	public void oldClose(OperationCallback h) {
		Channel realChannel = getRealChannel();
		realChannel.oldClose(new UtilPassThroughWriteHandler(this, h));
	}
	public void oldClose() {
		Channel realChannel = getRealChannel();
		realChannel.oldClose();
	}

	public FutureOperation close() {
		Channel realChannel = getRealChannel();
		return realChannel.close();
	}
	
	public InetSocketAddress getRemoteAddress() {
		Channel realChannel = getRealChannel();
		return realChannel.getRemoteAddress();
	}
	public boolean isConnected() {
		Channel realChannel = getRealChannel();
		return realChannel.isConnected();
	}

	public ChannelSession getSession() {
		Channel realChannel = getRealChannel();
		return realChannel.getSession();
	}
	  	
}
