package org.webpieces.http2client.impl;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.DataListener;

public class Http2ChannelProxyImpl implements Http2ChannelProxy {

	private TCPChannel channel;

	public Http2ChannelProxyImpl(TCPChannel channel) {
		this.channel = channel;
	}

	@Override
	public CompletableFuture<Void> write(ByteBuffer data) {
		return channel.write(data);
	}

	@Override
	public CompletableFuture<Void> connect(InetSocketAddress addr, DataListener listener) {
		if(addr == null)
			throw new IllegalArgumentException("addr cannot be null");
		
		return channel.connect(addr, listener);
	}

	@Override
	public CompletableFuture<Void> close() {
		return channel.close();
	}

}
