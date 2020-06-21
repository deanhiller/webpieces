package org.webpieces.nio.impl.threading;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.ChannelSession;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.util.threading.SessionExecutor;

public class ThreadChannel implements Channel {

	private ProxyExecutor executor;
	private Channel tcpChannel;
	private SessionExecutor sessionExecutor;

	public ThreadChannel(Channel channel, SessionExecutor executor2) {
		this.tcpChannel = channel;
		this.sessionExecutor = executor2;
		this.executor =  new ProxyExecutor(channel, executor2);
	}
	
	@Override
	public CompletableFuture<Void> connect(SocketAddress addr, DataListener listener) {
		DataListener threaded = new ThreadDataListener(this, listener, sessionExecutor);
		CompletableFuture<Void> future = tcpChannel.connect(addr, threaded);
		//transfer this to the SessionExecutor properly such that clients do
		//not need to synchronize the ChannelSession writes/reads
		return future.thenApplyAsync(p -> null, executor);
	}

	@Override
	public CompletableFuture<Void> write(ByteBuffer b) {
		CompletableFuture<Void> future = tcpChannel.write(b);
		//transfer this to the SessionExecutor properly such that clients do
		//not need to synchronize the ChannelSession writes/reads
		return future.thenApplyAsync(p -> null, executor);
	}

	@Override
	public CompletableFuture<Void> close() {
		CompletableFuture<Void> future = tcpChannel.close();
		//transfer this to the SessionExecutor properly such that clients do
		//not need to synchronize the ChannelSession writes/reads
		return future.thenApplyAsync(p -> null, executor);
	}

	public void setReuseAddress(boolean b) {
		tcpChannel.setReuseAddress(b);
	}

	@Override
	public String getChannelId() {
		return tcpChannel.getChannelId();
	}

	public CompletableFuture<Void> bind(SocketAddress addr) {
		return tcpChannel.bind(addr);
	}

	public boolean isBlocking() {
		return tcpChannel.isBlocking();
	}

	public boolean isClosed() {
		return tcpChannel.isClosed();
	}

	public boolean isBound() {
		return tcpChannel.isBound();
	}

	public InetSocketAddress getLocalAddress() {
		return tcpChannel.getLocalAddress();
	}

	public InetSocketAddress getRemoteAddress() {
		return tcpChannel.getRemoteAddress();
	}

	public boolean isConnected() {
		return tcpChannel.isConnected();
	}

	public ChannelSession getSession() {
		return tcpChannel.getSession();
	}

	@Override
	public boolean isSslChannel() {
		return tcpChannel.isSslChannel();
	}

	@Override
	public Boolean isServerSide() {
		return tcpChannel.isServerSide();
	}

}
