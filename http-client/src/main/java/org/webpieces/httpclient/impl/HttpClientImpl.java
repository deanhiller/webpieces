package org.webpieces.httpclient.impl;

import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.httpclient.api.HttpClient;
import org.webpieces.httpclient.api.HttpSocket;
import org.webpieces.httpclient.api.ResponseListener;
import org.webpieces.nio.api.ChannelManager;

import com.webpieces.httpparser.api.HttpParser;
import com.webpieces.httpparser.api.dto.HttpRequest;

public class HttpClientImpl implements HttpClient {

	private static final Logger log = LoggerFactory.getLogger(HttpClientImpl.class);
	private ChannelManager mgr;
	private HttpParser parser;

	public HttpClientImpl(ChannelManager mgr, HttpParser parser) {
		this.mgr = mgr;
		this.parser = parser;
	}

	@Override
	public void sendSingleRequest(SocketAddress addr, HttpRequest request, ResponseListener listener) {
		HttpSocket socket = openHttpSocket(request.getRequestLine()+"");

		CompletableFuture<HttpSocket> connect = socket.connect(addr);
		connect.thenAccept(p -> p.send(request, listener))
			.exceptionally(e -> fail(socket, listener, e));
	}

	private Void fail(HttpSocket socket, ResponseListener listener, Throwable e) {
		CompletableFuture<HttpSocket> closeSocket = socket.closeSocket();
		closeSocket.exceptionally(ee -> {
			log.warn("could not close socket due to exception");
			return socket;
		});
		listener.failure(e);
		return null;
	}

	@Override
	public HttpSocket openHttpSocket(String idForLogging) {
		return new HttpSocketImpl(mgr, idForLogging, parser);
	}

}
