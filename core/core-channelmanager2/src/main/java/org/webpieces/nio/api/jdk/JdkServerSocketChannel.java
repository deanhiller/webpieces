package org.webpieces.nio.api.jdk;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

public interface JdkServerSocketChannel {
//	public SelectorProvider provider();
//
	public void close() throws IOException;
//
//	public int validOps();
//
//	public java.nio.channels.ServerSocketChannel bind(SocketAddress local) throws IOException;
//
//	public boolean isRegistered();
//
	public SelectionKey keyFor();

	public SelectionKey register(int ops, Object att) throws ClosedChannelException;
//
//	public boolean isOpen();
//
//	public <T> T getOption(SocketOption<T> name) throws IOException;
//
//	public java.nio.channels.ServerSocketChannel bind(SocketAddress local, int backlog) throws IOException;
//
//	public Set<SocketOption<?>> supportedOptions();
//
//	public <T> java.nio.channels.ServerSocketChannel setOption(SocketOption<T> name, T value) throws IOException;
//
	public ServerSocket socket();

	public JdkSocketChannel accept() throws IOException;

	public boolean isBlocking();
//
//	public Object blockingLock();
//
	public SelectableChannel configureBlocking(boolean block) throws IOException;
//
//	public SelectionKey register(Selector sel, int ops) throws ClosedChannelException;
//
//	public SocketAddress getLocalAddress() throws IOException;

	public java.nio.channels.ServerSocketChannel getRealChannel();
	
	public void resetRegisteredOperations(int ops);
	
	public void setReuseAddress(boolean b) throws SocketException;
	
	public void bind(SocketAddress srvrAddr) throws IOException;
	
	public boolean isBound();
	
	public boolean isClosed();
	
	public InetSocketAddress getInetSocketAddress();

}
