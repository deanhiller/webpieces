package org.webpieces.webserver.test.http2.directfast;

import javax.net.ssl.SSLEngine;

import org.webpieces.http2client.api.Http2Client;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.http2client.api.Http2SocketListener;

public class DirectFastClient implements Http2Client {

	private MockFrontendManager frontEnd;

	public DirectFastClient(MockFrontendManager frontEnd) {
		this.frontEnd = frontEnd;
	}

	@Override
	public Http2Socket createHttpSocket(Http2SocketListener listener) {
		return new MockHttp2Socket(listener, frontEnd.getHttpListener(), false);
	}

	@Override
	public Http2Socket createHttpsSocket(SSLEngine factory, Http2SocketListener listener) {
		return new MockHttp2Socket(listener, frontEnd.getHttpsListener(), true);
	}

}
