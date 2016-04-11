package org.webpieces.asyncserver.impl;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.nio.api.handlers.ConnectionListener;

public class ProxyTcpServerChannel implements TCPServerChannel {

	private TCPServerChannel serverChannel;
	private ConnectedChannels connectedChannels;

	public ProxyTcpServerChannel(TCPServerChannel serverChannel, ConnectedChannels connectedChannels) {
		this.serverChannel = serverChannel;
		this.connectedChannels = connectedChannels;
	}
	
	public void registerServerSocketChannel(ConnectionListener listener) {
		serverChannel.registerServerSocketChannel(listener);
	}

	public void closeServerChannel() {
		serverChannel.closeServerChannel();
		
		for(TCPChannel channel : connectedChannels.getAllChannels()) {
			channel.close();
		}
	}

	public void setReuseAddress(boolean b) {
		serverChannel.setReuseAddress(b);
	}

	public void setName(String string) {
		serverChannel.setName(string);
	}

	public String getName() {
		return serverChannel.getName();
	}

	public void bind(SocketAddress addr) {
		serverChannel.bind(addr);
	}

	public boolean isBlocking() {
		return serverChannel.isBlocking();
	}

	public boolean isClosed() {
		return serverChannel.isClosed();
	}

	public boolean isBound() {
		return serverChannel.isBound();
	}

	public InetSocketAddress getLocalAddress() {
		return serverChannel.getLocalAddress();
	}

}
