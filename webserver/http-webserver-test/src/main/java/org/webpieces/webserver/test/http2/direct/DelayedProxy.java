package org.webpieces.webserver.test.http2.direct;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.webpieces.nio.api.channels.HostWithPort;
import org.webpieces.util.futures.XFuture;

import org.webpieces.http2client.impl.Http2ChannelProxy;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.util.context.Context;
import org.webpieces.webserver.test.MockTcpChannel;

public class DelayedProxy implements Http2ChannelProxy {

	private ConnectionListener listener;
	private MockTcpChannel channel;
	private DataListener toServerDataListener;

	public DelayedProxy(ConnectionListener listener, MockTcpChannel channel) {
		this.listener = listener;
		this.channel = channel;
	}

	@Override
	public XFuture<Void> connect(HostWithPort addr, DataListener dataListener) {
		channel.setDataListener(dataListener);
		return listener.connected(channel, true).thenApply( d -> {
			toServerDataListener = d;
			return null;
		});
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	@Override
	public XFuture<Void> connect(InetSocketAddress addr, DataListener dataListener) {
		channel.setDataListener(dataListener);
		return listener.connected(channel, true).thenApply( d -> {
			toServerDataListener = d;
			return null;
		});
	}

	@Override
	public XFuture<Void> write(ByteBuffer buffer) {
		Map<String, Object> previous = Context.copyContext();
		//no context server side...
		Context.setContext(new HashMap<>());
		try {
			return toServerDataListener.incomingData(channel, buffer);
		} finally {
			Context.setContext(previous);
		}
	}

	@Override
	public XFuture<Void> close() {
		toServerDataListener.farEndClosed(channel);
		return XFuture.completedFuture(null);
	}


}
