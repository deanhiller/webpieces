package org.webpieces.httpfrontend2.api.mock2;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import org.webpieces.util.futures.XFuture;

import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.nio.api.handlers.ConsumerFunc;

public class MockTcpServerChannel implements TCPServerChannel {

	@Override
	public void setReuseAddress(boolean b) {
		
	}

	@Override
	public String getChannelId() {
		return null;
	}

	@Override
	public XFuture<Void> bind(SocketAddress addr) {
		return XFuture.completedFuture(null);
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

}
