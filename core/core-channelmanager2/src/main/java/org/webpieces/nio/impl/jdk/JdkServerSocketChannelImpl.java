package org.webpieces.nio.impl.jdk;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.webpieces.nio.api.jdk.JdkServerSocketChannel;

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

	public SocketChannel accept() throws IOException {
		return channel.accept();
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
	
}
