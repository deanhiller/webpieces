package org.webpieces.httpfrontend2.api.adaptor;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;

import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.ConsumerFunc;

class AdaptorServerChannel implements TCPServerChannel {

	private String name;
	private String id;
	private ConnectionListener connectionListener;
	private ServerChannels svrChannels2;

	public AdaptorServerChannel(String id, ConnectionListener connectionListener, ServerChannels svrChannels) {
		this.id = id;
		this.connectionListener = connectionListener;
		svrChannels2 = svrChannels;
	}

	@Override
	public void setReuseAddress(boolean b) {
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getChannelId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void bind(SocketAddress addr) {
		svrChannels2.bindTo(addr, this);
	}

	@Override
	public boolean isBlocking() {
		return false;
	}

	@Override
	public boolean isClosed() {
		return false;
	}

	@Override
	public boolean isBound() {
		return false;
	}

	@Override
	public InetSocketAddress getLocalAddress() {
		return null;
	}

	@Override
	public void closeServerChannel() {
	}

	@Override
	public void configure(ConsumerFunc<ServerSocketChannel> methodToConfigure) {
	}

	@Override
	public ServerSocketChannel getUnderlyingChannel() {
		return null;
	}

	public ConnectionListener getConnectionListener() {
		return connectionListener;
	}
}