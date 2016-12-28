package org.webpieces.httpclient.impl2;

import java.nio.ByteBuffer;

import org.webpieces.httpclient.api2.HttpServerListener;
import org.webpieces.httpclient.api2.HttpSocket;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.DataListener;

public class SocketDataListener implements DataListener {

	private HttpServerListener listener;
	private HttpSocket socket;

	public SocketDataListener(HttpSocket socket, HttpServerListener listener) {
		this.socket = socket;
		this.listener = listener;
	}

	@Override
	public void incomingData(Channel channel, ByteBuffer b) {
		
	}

	@Override
	public void farEndClosed(Channel channel) {
		listener.farEndClosed(socket);
	}

	@Override
	public void failure(Channel channel, ByteBuffer data, Exception e) {
		listener.failure(e);
	}

	@Override
	public void applyBackPressure(Channel channel) {
	}

	@Override
	public void releaseBackPressure(Channel channel) {
	}

}
