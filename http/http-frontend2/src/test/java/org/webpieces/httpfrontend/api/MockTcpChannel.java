package org.webpieces.httpfrontend.api;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.ChannelSession;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.nio.impl.util.ChannelSessionImpl;

class MockTcpChannel implements TCPChannel {

	private boolean isClosed;
	private ChannelSessionImpl session = new ChannelSessionImpl();
	private LinkedList<ByteBuffer> writeLog = new LinkedList<>();

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
	}

	@Override
	public boolean isBlocking() {
		return false;
	}

	@Override
	public boolean isClosed() {
		return isClosed;
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
	public CompletableFuture<Channel> connect(SocketAddress addr, DataListener listener) {
		return null;
	}

	@Override
	public CompletableFuture<Channel> write(ByteBuffer b) {
		writeLog.add(b);
		return CompletableFuture.completedFuture(this);
	}

	@Override
	public CompletableFuture<Channel> close() {
		isClosed = true;
		return CompletableFuture.completedFuture(this);
	}

	@Override
	public CompletableFuture<Channel> registerForReads() {
		return null;
	}

	@Override
	public CompletableFuture<Channel> unregisterForReads() {
		return null;
	}

	@Override
	public boolean isRegisteredForReads() {
		return false;
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		return null;
	}

	@Override
	public boolean isConnected() {
		return false;
	}

	@Override
	public ChannelSession getSession() {
		return session;
	}

	@Override
	public void setMaxBytesWriteBackupSize(int maxBytesBackup) {
	}

	@Override
	public int getMaxBytesBackupSize() {
		return 0;
	}

	@Override
	public boolean getKeepAlive() {
		return false;
	}

	@Override
	public void setKeepAlive(boolean b) {
		
	}

	@Override
	public boolean isSslChannel() {
		return false;
	}

	public ByteBuffer getWriteLog() {
		ByteBuffer fullLog = ByteBuffer.allocate(writeLog.stream().map(b -> b.limit() - b.position()).reduce(0, (a, b) -> a+b));
		for(ByteBuffer b: writeLog) {
			fullLog.put(b);
		}
		fullLog.flip();
		return fullLog;
	}
}
