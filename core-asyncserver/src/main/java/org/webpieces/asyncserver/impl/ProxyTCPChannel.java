package org.webpieces.asyncserver.impl;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

import org.webpieces.nio.api.channels.ChannelSession;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.nio.api.handlers.FutureOperation;

public class ProxyTCPChannel implements TCPChannel {

	private TCPChannel channel;
	private ConnectedChannels connectedChannels;

	public ProxyTCPChannel(TCPChannel channel, ConnectedChannels connectedChannels) {
		this.channel = channel;
		this.connectedChannels = connectedChannels;
	}

	public FutureOperation connect(SocketAddress addr) {
		return channel.connect(addr);
	}

	public FutureOperation write(ByteBuffer b) {
		return channel.write(b);
	}

	public FutureOperation close() {
		//technically we are not closed until FutureOperation does it's callback, but remove because we also
		//do not need to call close a second time...
		connectedChannels.removeChannel(channel);
		return channel.close();
	}

	public void registerForReads(DataListener listener) {
		channel.registerForReads(listener);
	}

	public void setReuseAddress(boolean b) {
		channel.setReuseAddress(b);
	}

	public void unregisterForReads() {
		channel.unregisterForReads();
	}

	public boolean getKeepAlive() {
		return channel.getKeepAlive();
	}

	public void setKeepAlive(boolean b) {
		channel.setKeepAlive(b);
	}

	public void setName(String string) {
		channel.setName(string);
	}

	public InetSocketAddress getRemoteAddress() {
		return channel.getRemoteAddress();
	}

	public boolean isConnected() {
		return channel.isConnected();
	}

	public String getName() {
		return channel.getName();
	}

	public void bind(SocketAddress addr) {
		channel.bind(addr);
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

	@Override
	public ChannelSession getSession() {
		return channel.getSession();
	}

}
