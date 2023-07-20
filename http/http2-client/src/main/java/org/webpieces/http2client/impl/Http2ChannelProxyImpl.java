package org.webpieces.http2client.impl;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.webpieces.nio.api.channels.HostWithPort;
import org.webpieces.util.futures.XFuture;

import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.DataListener;

public class Http2ChannelProxyImpl implements Http2ChannelProxy {

	private TCPChannel channel;

	public Http2ChannelProxyImpl(TCPChannel channel) {
		this.channel = channel;
	}

	@Override
	public XFuture<Void> write(ByteBuffer data) {
		return channel.write(data);
	}

	@Override
	public XFuture<Void> connect(HostWithPort addr, DataListener listener) {
		if(addr == null)
			throw new IllegalArgumentException("addr cannot be null");

		return channel.connect(addr, listener);
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	@Override
	public XFuture<Void> connect(InetSocketAddress addr, DataListener listener) {
		if(addr == null)
			throw new IllegalArgumentException("addr cannot be null");
		
		return channel.connect(addr, listener);
	}

	@Override
	public XFuture<Void> close() {
		return channel.close();
	}

	@Override
	public String toString() {
		return channel + "";
	}
	
}
