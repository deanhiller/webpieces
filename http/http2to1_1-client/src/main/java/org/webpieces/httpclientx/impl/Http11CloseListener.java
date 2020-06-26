package org.webpieces.httpclientx.impl;

import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.http2client.api.Http2SocketListener;
import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.httpclient11.api.HttpSocketListener;

public class Http11CloseListener implements HttpSocketListener {

	private Http2SocketListener listener;
	private Http2Socket http2Socket;

	public Http11CloseListener(Http2SocketListener listener) {
		this.listener = listener;
	}

	@Override
	public void socketClosed(HttpSocket socket) {
		listener.socketFarEndClosed(http2Socket);
	}

	public void setHttp2Socket(Http2SocketImpl http2Socket) {
		this.http2Socket = http2Socket;
	}

}
