package org.webpieces.nio.impl.ssl;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import org.webpieces.util.futures.XFuture;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.ChannelSession;

public abstract class SslChannel implements Channel {

	private final Channel channel;

	public SslChannel(Channel realChannel) {
		this.channel = realChannel;
	}
	
	public void setReuseAddress(boolean b) {
		channel.setReuseAddress(b);
	}

	@Override
	public String getChannelId() {
		return channel.getChannelId();
	}

	public XFuture<Void> bind(SocketAddress addr) {
		return channel.bind(addr);
	}

	public boolean isBlocking() {
		return channel.isBlocking();
	}

	public boolean isClosed() {
		return channel.isClosed();
	}

	public boolean isBound() {
		return channel.isBound();
	}

	public InetSocketAddress getLocalAddress() {
		return channel.getLocalAddress();
	}

	public InetSocketAddress getRemoteAddress() {
		return channel.getRemoteAddress();
	}

	public boolean isConnected() {
		return channel.isConnected();
	}

	public ChannelSession getSession() {
		return channel.getSession();
	}

	@Override
	public String toString() {
		return "ssl" + channel;
	}

}
