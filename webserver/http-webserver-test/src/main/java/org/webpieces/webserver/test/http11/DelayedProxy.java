package org.webpieces.webserver.test.http11;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.httpclient11.impl.ChannelProxy;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.webserver.test.MockTcpChannel;

public class DelayedProxy implements ChannelProxy {

	private ConnectionListener listener;
	private MockTcpChannel channel;
	private DataListener toServerDataListener;

	public DelayedProxy(ConnectionListener listener, MockTcpChannel channel) {
		this.listener = listener;
		this.channel = channel;
	}

	@Override
	public CompletableFuture<Void> connect(InetSocketAddress addr, DataListener dataListener) {
		channel.setDataListener(dataListener);
		return listener.connected(channel, true).thenApply( d -> {
			toServerDataListener = d;
			return null;
		});
	}

	@Override
	public CompletableFuture<Void> write(ByteBuffer buffer) {
		return toServerDataListener.incomingData(channel, buffer);
	}

	@Override
	public CompletableFuture<Void> close() {
		toServerDataListener.farEndClosed(channel);
		return CompletableFuture.completedFuture(null);
	}

}
