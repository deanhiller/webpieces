package org.webpieces.nio.api.jdk;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

public interface JdkDatagramChannel {

//	public SelectorProvider provider();
//
	public void close() throws IOException;
//
//	public boolean isRegistered();

	public SelectionKey keyFor();

	public SelectionKey register(int ops, Object att) throws ClosedChannelException;

	public boolean isOpen();
//
//	public <T> T getOption(SocketOption<T> name) throws IOException;
//
//	public Set<SocketOption<?>> supportedOptions();
//
//	public MembershipKey join(InetAddress group, NetworkInterface interf) throws IOException;
//
//	public int validOps();
//
//	public java.nio.channels.DatagramChannel bind(SocketAddress local) throws IOException;
//
//	public MembershipKey join(InetAddress group, NetworkInterface interf, InetAddress source) throws IOException;
//
//	public <T> java.nio.channels.DatagramChannel setOption(SocketOption<T> name, T value) throws IOException;
//
	public boolean isBlocking();
//
//	public Object blockingLock();

	public SelectableChannel configureBlocking(boolean block) throws IOException;

	public DatagramSocket socket();
//
//	public SelectionKey register(int ops) throws ClosedChannelException;
//
	public boolean isConnected();
//
	public java.nio.channels.DatagramChannel connect(SocketAddress remote) throws IOException;

	public java.nio.channels.DatagramChannel disconnect() throws IOException;
//
//	public SocketAddress getRemoteAddress() throws IOException;
//
//	public SocketAddress receive(ByteBuffer dst) throws IOException;
//
//	public int send(ByteBuffer src, SocketAddress target) throws IOException;
//
	public int read(ByteBuffer dst) throws IOException;
//
//	public long read(ByteBuffer[] dsts, int offset, int length) throws IOException;
//
//	public long read(ByteBuffer[] dsts) throws IOException;
//
	public int write(ByteBuffer src) throws IOException;
//
//	public long write(ByteBuffer[] srcs, int offset, int length) throws IOException;
//
//	public long write(ByteBuffer[] srcs) throws IOException;
//
//	public SocketAddress getLocalAddress() throws IOException;

	public void resetRegisteredOperations(int ops);
}
