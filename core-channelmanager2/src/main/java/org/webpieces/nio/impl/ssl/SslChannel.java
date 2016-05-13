package org.webpieces.nio.impl.ssl;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.ChannelSession;

public abstract class SslChannel implements Channel {

	private final Channel channel;

	public SslChannel(Channel realChannel) {
		this.channel = realChannel;
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

	public CompletableFuture<Channel> registerForReads() {
		return channel.registerForReads().thenApply(c -> this);
	}

	public CompletableFuture<Channel> unregisterForReads() {
		return channel.unregisterForReads().thenApply(c -> this);
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

}
