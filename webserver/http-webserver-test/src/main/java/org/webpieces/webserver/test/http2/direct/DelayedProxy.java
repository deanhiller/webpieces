package org.webpieces.webserver.test.http2.direct;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.webpieces.http2client.impl.Http2ChannelProxy;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.util.context.Context;
import org.webpieces.webserver.test.MockTcpChannel;

public class DelayedProxy implements Http2ChannelProxy {

	private static final String IS_SERVER_SIDE = "_isServerSide";
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
		Boolean isServerSide = (Boolean) Context.get(IS_SERVER_SIDE);

		Map<String, Object> context = Context.copyContext();
		Context.put(IS_SERVER_SIDE, Boolean.TRUE);
		try {
			return toServerDataListener.incomingData(channel, buffer);
		} finally {
			if(isServerSide == null) {
				//We must simulate being separate from the webserver and the webserver sets and
				//clears the context so we need to capture context and restore it here for tests
				//since everything is single threaded, the server loops around in which case, we
				//do not want to touch the server's context
				Context.restoreContext(context);
			}
		}
	}

	@Override
	public CompletableFuture<Void> close() {
		toServerDataListener.farEndClosed(channel);
		return CompletableFuture.completedFuture(null);
	}


}
