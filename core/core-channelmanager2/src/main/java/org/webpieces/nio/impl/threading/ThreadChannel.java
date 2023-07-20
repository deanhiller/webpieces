package org.webpieces.nio.impl.threading;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

import org.webpieces.nio.api.channels.HostWithPort;
import org.webpieces.util.futures.XFuture;

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
	public XFuture<Void> connect(HostWithPort addr, DataListener listener) {
		DataListener threaded = new ThreadDataListener(this, listener, sessionExecutor);
		XFuture<Void> future = tcpChannel.connect(addr, threaded);
		//transfer this to the SessionExecutor properly such that clients do
		//not need to synchronize the ChannelSession writes/reads
		return future.thenApplyAsync(p -> null, executor);
	}

	/**
	 * @deprecated Use connect(HostWithPort, DataListener) or connect(IpWithPort, DataListener) instead
	 */
	@Deprecated
	@Override
	public XFuture<Void> connect(SocketAddress addr, DataListener listener) {
		DataListener threaded = new ThreadDataListener(this, listener, sessionExecutor);
		XFuture<Void> future = tcpChannel.connect(addr, threaded);
		//transfer this to the SessionExecutor properly such that clients do
		//not need to synchronize the ChannelSession writes/reads
		return future.thenApplyAsync(p -> null, executor);
	}

	@Override
	public XFuture<Void> write(ByteBuffer b) {
		XFuture<Void> future = tcpChannel.write(b);
		//transfer this to the SessionExecutor properly such that clients do
		//not need to synchronize the ChannelSession writes/reads
		return future.thenApplyAsync(p -> null, executor);
	}

	@Override
	public XFuture<Void> close() {
		XFuture<Void> future = tcpChannel.close();
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

	public XFuture<Void> bind(SocketAddress addr) {
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
