package org.webpieces.webserver.test.http2.directfast;

import org.webpieces.http2client.api.Http2SocketListener;

public class ProxyClose {

	private Http2SocketListener closeListener;
	private MockHttp2Socket mockHttp2Socket;

	public ProxyClose(Http2SocketListener closeListener, MockHttp2Socket mockHttp2Socket) {
		this.closeListener = closeListener;
		this.mockHttp2Socket = mockHttp2Socket;
	}

	public void socketFarEndClosed() {
		closeListener.socketFarEndClosed(mockHttp2Socket);
	}

}
