package org.webpieces.asyncserver.impl;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.ChannelSession;
import org.webpieces.nio.api.channels.TCPChannel;

public class ProxyTCPChannel implements TCPChannel {

	private TCPChannel channel;
	private ConnectedChannels connectedChannels;

	public ProxyTCPChannel(TCPChannel channel, ConnectedChannels connectedChannels) {
		this.channel = channel;
		this.connectedChannels = connectedChannels;
	}

	public CompletableFuture<Channel> connect(SocketAddress addr) {
		return channel.connect(addr);
	}

	public CompletableFuture<Channel> write(ByteBuffer b) {
		return channel.write(b);
	}

	public CompletableFuture<Channel> close() {
		//technically we are not closed until FutureOperation does it's callback, but remove because we also
		//do not need to call close a second time...
		connectedChannels.removeChannel(channel);
		return channel.close();
	}

	public void registerForReads() {
		channel.registerForReads();
	}

	public void setReuseAddress(boolean b) {
		channel.setReuseAddress(b);
	}

	public void unregisterForReads() {
		channel.unregisterForReads();
	}

	public boolean getKeepAlive() {
		return channel.getKeepAlive();
	}

	public void setKeepAlive(boolean b) {
		channel.setKeepAlive(b);
	}

	public void setName(String string) {
		channel.setName(string);
	}

	public InetSocketAddress getRemoteAddress() {
		return channel.getRemoteAddress();
	}

	public boolean isConnected() {
		return channel.isConnected();
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

	@Override
	public ChannelSession getSession() {
		return channel.getSession();
	}

	@Override
	public void setWriteTimeoutMs(int timeout) {
		channel.setWriteTimeoutMs(timeout);
	}

	@Override
	public int getWriteTimeoutMs() {
		return channel.getWriteTimeoutMs();
	}

	public void setMaxBytesWriteBackupSize(int maxBytesBackup) {
		channel.setMaxBytesWriteBackupSize(maxBytesBackup);
	}

	public int getMaxBytesBackupSize() {
		return channel.getMaxBytesBackupSize();
	}

	public boolean isRegisteredForReads() {
		return channel.isRegisteredForReads();
	}

	public boolean isFailOnNoBackPressure() {
		return channel.isFailOnNoBackPressure();
	}

	public void setFailOnNoBackPressure(boolean failOnNoBackPressure) {
		channel.setFailOnNoBackPressure(failOnNoBackPressure);
	}

}
