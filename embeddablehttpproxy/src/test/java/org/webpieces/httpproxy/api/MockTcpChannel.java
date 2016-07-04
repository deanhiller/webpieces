package org.webpieces.httpproxy.api;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.ChannelSession;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.nio.impl.util.ChannelSessionImpl;

public class MockTcpChannel implements TCPChannel {

	private boolean isClosed;
	private ChannelSessionImpl session = new ChannelSessionImpl();

	@Override
	public void setReuseAddress(boolean b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setName(String string) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getChannelId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void bind(SocketAddress addr) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isBlocking() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isClosed() {
		return isClosed;
	}

	@Override
	public boolean isBound() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public InetSocketAddress getLocalAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Channel> connect(SocketAddress addr, DataListener listener) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Channel> write(ByteBuffer b) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Channel> close() {
		isClosed = true;
		return CompletableFuture.completedFuture(this);
	}

	@Override
	public CompletableFuture<Channel> registerForReads() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Channel> unregisterForReads() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isRegisteredForReads() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ChannelSession getSession() {
		return session;
	}

	@Override
	public void setWriteTimeoutMs(int timeout) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getWriteTimeoutMs() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setMaxBytesWriteBackupSize(int maxBytesBackup) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getMaxBytesBackupSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean getKeepAlive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setKeepAlive(boolean b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isSslChannel() {
		return false;
	}


	

}
