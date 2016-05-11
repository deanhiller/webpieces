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
	private Channel channel;
	private SessionExecutor sessionExecutor;

	public ThreadChannel(Channel channel, SessionExecutor executor2) {
		this.channel = channel;
		this.sessionExecutor = executor2;
		this.executor =  new ProxyExecutor(channel, executor2);
	}
	
	@Override
	public CompletableFuture<Channel> connect(SocketAddress addr, DataListener listener) {
		DataListener threaded = new ThreadDataListener(listener, sessionExecutor);
		CompletableFuture<Channel> future = channel.connect(addr, threaded);
		//transfer this to the SessionExecutor properly such that clients do
		//not need to synchronize the ChannelSession writes/reads
		return future.thenApplyAsync(p -> p, executor);
	}

	@Override
	public CompletableFuture<Channel> write(ByteBuffer b) {
		CompletableFuture<Channel> future = channel.write(b);
		//transfer this to the SessionExecutor properly such that clients do
		//not need to synchronize the ChannelSession writes/reads
		return future.thenApplyAsync(p -> p, executor);
	}

	@Override
	public CompletableFuture<Channel> close() {
		CompletableFuture<Channel> future = channel.close();
		//transfer this to the SessionExecutor properly such that clients do
		//not need to synchronize the ChannelSession writes/reads
		return future.thenApplyAsync(p -> p, executor);
	}

	public void setReuseAddress(boolean b) {
		channel.setReuseAddress(b);
	}

	public void setName(String string) {
		channel.setName(string);
	}

	public String getName() {
		return channel.getName();
	}

	public void bind(SocketAddress addr) {
		channel.bind(addr);
	}

	public boolean isBlocking() {
		return channel.isBlocking();
	}

	public boolean isClosed() {
		return channel.isClosed();
	}

	public boolean isBound() {
		return channel.isBound();
	}

	public InetSocketAddress getLocalAddress() {
		return channel.getLocalAddress();
	}

	public void registerForReads() {
		channel.registerForReads();
	}

	public void unregisterForReads() {
		channel.unregisterForReads();
	}

	public boolean isRegisteredForReads() {
		return channel.isRegisteredForReads();
	}

	public InetSocketAddress getRemoteAddress() {
		return channel.getRemoteAddress();
	}

	public boolean isConnected() {
		return channel.isConnected();
	}

	public ChannelSession getSession() {
		return channel.getSession();
	}

	public void setWriteTimeoutMs(int timeout) {
		channel.setWriteTimeoutMs(timeout);
	}

	public int getWriteTimeoutMs() {
		return channel.getWriteTimeoutMs();
	}

	public void setMaxBytesWriteBackupSize(int maxBytesBackup) {
		channel.setMaxBytesWriteBackupSize(maxBytesBackup);
	}

	public int getMaxBytesBackupSize() {
		return channel.getMaxBytesBackupSize();
	}

	public boolean isFailOnNoBackPressure() {
		return channel.isFailOnNoBackPressure();
	}

	public void setFailOnNoBackPressure(boolean failOnNoBackPressure) {
		channel.setFailOnNoBackPressure(failOnNoBackPressure);
	}

	
}
