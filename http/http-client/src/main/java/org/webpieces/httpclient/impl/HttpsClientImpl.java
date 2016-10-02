package org.webpieces.httpclient.impl;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import org.webpieces.httpclient.api.CloseListener;
import org.webpieces.httpclient.api.HttpClient;
import org.webpieces.httpclient.api.HttpSocket;
import org.webpieces.httpclient.api.HttpsSslEngineFactory;
import org.webpieces.httpclient.api.ResponseListener;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.nio.api.ChannelManager;

public class HttpsClientImpl implements HttpClient {

	private static final Logger log = LoggerFactory.getLogger(HttpClientImpl.class);
	private ChannelManager mgr;
	private HttpParser parser;
	private HttpsSslEngineFactory factory;

	public HttpsClientImpl(ChannelManager mgr, HttpParser parser, HttpsSslEngineFactory factory) {
		this.mgr = mgr;
		this.parser = parser;
		this.factory = factory;
	}

	@Override
	public CompletableFuture<HttpResponse> sendSingleRequest(InetSocketAddress addr, HttpRequest request) {
		HttpSocket socket = openHttpSocket(addr+"");
		CompletableFuture<HttpSocket> connect = socket.connect(addr);
		return connect.thenCompose(p -> socket.send(request));
	}
	
	@Override
	public void sendSingleRequest(InetSocketAddress addr, HttpRequest request, ResponseListener listener) {
		HttpSocket socket = openHttpSocket(addr+"");

		CompletableFuture<HttpSocket> connect = socket.connect(addr);
		connect.thenAccept(p -> socket.send(request, listener))
			.exceptionally(e -> fail(socket, listener, e));
	}

	private Void fail(HttpSocket socket, ResponseListener listener, Throwable e) {
		CompletableFuture<HttpSocket> closeSocket = socket.closeSocket();
		closeSocket.exceptionally(ee -> {
			log.error("could not close socket due to exception");
			return socket;
		});
		listener.failure(e);
		return null;
	}

	@Override
	public HttpSocket openHttpSocket(String idForLogging) {
		return openHttpSocket(idForLogging, null);
	}
	
	@Override
	public HttpSocket openHttpSocket(String idForLogging, CloseListener listener) {
		return new HttpSocketImpl(mgr, idForLogging, factory, parser, listener);
	}


}
