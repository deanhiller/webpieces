package org.webpieces.nio.impl.jdk;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import org.webpieces.nio.api.jdk.JdkDatagramChannel;

public class JdkDatagramChannelImpl implements JdkDatagramChannel {

	private java.nio.channels.DatagramChannel channel;
	private Selector selector;

	public JdkDatagramChannelImpl(java.nio.channels.DatagramChannel channel, Selector selector) {
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
	
	public final boolean isOpen() {
		return channel.isOpen();
	}

	public final boolean isBlocking() {
		return channel.isBlocking();
	}

	public final SelectableChannel configureBlocking(boolean block) throws IOException {
		return channel.configureBlocking(block);
	}

	public DatagramSocket socket() {
		return channel.socket();
	}

	public boolean isConnected() {
		return channel.isConnected();
	}

	public DatagramChannel connect(SocketAddress remote) throws IOException {
		return channel.connect(remote);
	}

	public DatagramChannel disconnect() throws IOException {
		return channel.disconnect();
	}

	public int read(ByteBuffer dst) throws IOException {
		return channel.read(dst);
	}

	public int write(ByteBuffer src) throws IOException {
		return channel.write(src);
	}

}
