package org.webpieces.httpclientx.impl;

import javax.net.ssl.SSLEngine;

import org.webpieces.http2client.api.Http2Client;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.httpclient11.api.HttpClient;
import org.webpieces.httpclient11.api.HttpSocket;

public class Http2ClientProxy implements Http2Client {

	private HttpClient client11;

	public Http2ClientProxy(HttpClient client11) {
		this.client11 = client11;
	}

	@Override
	public Http2Socket createHttpSocket() {
		HttpSocket socket11 = client11.createHttpSocket();
		return new Http2SocketImpl(socket11);
	}

	@Override
	public Http2Socket createHttpsSocket(SSLEngine engine) {
		HttpSocket socket11 = client11.createHttpsSocket(engine);
		return new Http2SocketImpl(socket11);
	}

}
