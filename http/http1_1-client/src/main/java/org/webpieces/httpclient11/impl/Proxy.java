package org.webpieces.httpclient11.impl;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.webpieces.nio.api.channels.HostWithPort;
import org.webpieces.util.futures.XFuture;

import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.DataListener;

public class Proxy implements ChannelProxy {

	private TCPChannel channel;

	public Proxy(TCPChannel channel) {
		this.channel = channel;
	}

	@Override
	public XFuture<Void> connect(HostWithPort addr, DataListener dataListener) {
		if(addr == null)
			throw new IllegalArgumentException("addr cannot be null");
		return channel.connect(addr, dataListener);
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	@Override
	public XFuture<Void> connect(InetSocketAddress addr, DataListener dataListener) {
		if(addr == null)
			throw new IllegalArgumentException("addr cannot be null");
		return channel.connect(addr, dataListener);
	}

	@Override
	public XFuture<Void> write(ByteBuffer wrap) {
		return channel.write(wrap);
	}

	@Override
	public XFuture<Void> close() {
		return channel.close();
	}

	@Override
	public String getId() {
		return channel.getChannelId();
	}

	@Override
	public boolean isSecure() {
		return channel.isSslChannel();
	}

	
}
