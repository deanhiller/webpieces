package org.webpieces.httpclientx.impl;

import javax.net.ssl.SSLEngine;

import org.webpieces.http2client.api.Http2Client;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.httpclient11.api.HttpClient;
import org.webpieces.httpclient11.api.HttpSocket;

public class Http2ClientProxy implements Http2Client {

	private HttpClient client1_1;

	public Http2ClientProxy(HttpClient client1_1) {
		this.client1_1 = client1_1;
	}

	@Override
	public Http2Socket createHttpSocket() {
		HttpSocket socket1_1 = client1_1.createHttpSocket();
		return new Http2SocketImpl(socket1_1);
	}

	@Override
	public Http2Socket createHttpsSocket(SSLEngine factory) {
		HttpSocket socket1_1 = client1_1.createHttpSocket();
		return new Http2SocketImpl(socket1_1);
	}

}
