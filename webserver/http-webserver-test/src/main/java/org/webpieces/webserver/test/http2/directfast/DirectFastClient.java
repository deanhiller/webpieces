package org.webpieces.webserver.test.http2.directfast;

import javax.net.ssl.SSLEngine;

import org.webpieces.http2client.api.Http2Client;
import org.webpieces.http2client.api.Http2Socket;

public class DirectFastClient implements Http2Client {

	private MockFrontendManager frontEnd;

	public DirectFastClient(MockFrontendManager frontEnd) {
		this.frontEnd = frontEnd;
	}

	@Override
	public Http2Socket createHttpSocket() {
		return new MockHttp2Socket(frontEnd.getHttpListener(), false);
	}

	@Override
	public Http2Socket createHttpsSocket(SSLEngine factory) {
		return new MockHttp2Socket(frontEnd.getHttpsListener(), true);
	}

}
