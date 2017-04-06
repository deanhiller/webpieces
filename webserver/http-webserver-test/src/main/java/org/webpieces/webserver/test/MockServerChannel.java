package org.webpieces.webserver.test;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;

import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.nio.api.handlers.ConsumerFunc;

public class MockServerChannel implements TCPServerChannel {

	private InetSocketAddress addr;

	@Override
	public void setReuseAddress(boolean b) {
		
	}

	@Override
	public void setName(String string) {
		
	}

	@Override
	public String getChannelId() {
		return null;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public void bind(SocketAddress addr) {
		this.addr = (InetSocketAddress) addr;
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
		return addr;
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

}
