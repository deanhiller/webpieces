package org.webpieces.httpclientx.impl;

import javax.net.ssl.SSLEngine;

import org.webpieces.http2client.api.Http2Client;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.http2client.api.Http2SocketListener;
import org.webpieces.httpclient11.api.HttpClient;
import org.webpieces.httpclient11.api.HttpSocket;

public class Http2ClientProxy implements Http2Client {

	private HttpClient client11;

	public Http2ClientProxy(HttpClient client11) {
		this.client11 = client11;
	}

	@Override
	public Http2Socket createHttpSocket(Http2SocketListener listener) {
		Http11CloseListener http11Listener = new Http11CloseListener(listener);
		HttpSocket socket11 = client11.createHttpSocket(http11Listener);
		Http2SocketImpl http2Socket = new Http2SocketImpl(socket11);
		http11Listener.setHttp2Socket(http2Socket);
		return http2Socket;
	}

	@Override
	public Http2Socket createHttpsSocket(SSLEngine engine, Http2SocketListener listener) {
		Http11CloseListener http11Listener = new Http11CloseListener(listener);
		HttpSocket socket11 = client11.createHttpsSocket(engine, http11Listener);
		Http2SocketImpl http2Socket = new Http2SocketImpl(socket11);
		http11Listener.setHttp2Socket(http2Socket);
		return http2Socket;
	}

}
