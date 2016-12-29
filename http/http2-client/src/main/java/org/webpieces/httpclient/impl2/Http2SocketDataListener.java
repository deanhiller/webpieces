package org.webpieces.httpclient.impl2;

import java.nio.ByteBuffer;

import org.webpieces.httpclient.api.Http2ServerListener;
import org.webpieces.httpclient.api.Http2Socket;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.DataListener;

public class Http2SocketDataListener implements DataListener {

	private Http2ServerListener listener;
	private Http2Socket socket;

	public Http2SocketDataListener(Http2Socket socket, Http2ServerListener listener) {
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
