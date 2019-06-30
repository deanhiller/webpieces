package org.webpieces.nio.impl.jdk;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.webpieces.nio.api.jdk.JdkServerSocketChannel;
import org.webpieces.nio.api.jdk.JdkSocketChannel;

public class JdkServerSocketChannelImpl implements JdkServerSocketChannel {

	private java.nio.channels.ServerSocketChannel channel;
	private Selector selector;

	public JdkServerSocketChannelImpl(java.nio.channels.ServerSocketChannel channel, Selector selector) {
		this.channel = channel;
		this.selector = selector;
	}

	public final void close() throws IOException {
		channel.close();
	}

	public final SelectionKey keyFor() {
		return channel.keyFor(selector);
	}

	public final SelectionKey register(int ops, Object att) throws ClosedChannelException {
		return channel.register(selector, ops, att);
	}
	
	@Override
	public void resetRegisteredOperations(int ops) {
		SelectionKey key = keyFor();
		key.interestOps(ops);
	}
	
	public ServerSocket socket() {
		return channel.socket();
	}

	public JdkSocketChannel accept() throws IOException {
		SocketChannel s = channel.accept();
		if(s == null)
			return null;
		return new JdkSocketChannelImpl(s, selector);
	}

	public final boolean isBlocking() {
		return channel.isBlocking();
	}

	public final SelectableChannel configureBlocking(boolean block) throws IOException {
		return channel.configureBlocking(block);
	}

	@Override
	public ServerSocketChannel getRealChannel() {
		return channel;
	}

	@Override
	public void setReuseAddress(boolean b) throws SocketException {
		channel.socket().setReuseAddress(b);
	}

	@Override
	public void bind(SocketAddress srvrAddr) throws IOException {
		channel.socket().bind(srvrAddr);
	}

	@Override
	public boolean isBound() {
		return channel.socket().isBound();
	}

	@Override
	public boolean isClosed() {
		return channel.socket().isClosed();
	}

	@Override
	public InetSocketAddress getInetSocketAddress() {
		InetAddress addr = channel.socket().getInetAddress();
		int port = channel.socket().getLocalPort();
		return new InetSocketAddress(addr, port);
	}
	
}
