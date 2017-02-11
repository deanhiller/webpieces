package org.webpieces.httpclient.impl;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.httpclient.api.CloseListener;
import org.webpieces.httpclient.api.HttpChunkWriter;
import org.webpieces.httpclient.api.HttpClient;
import org.webpieces.httpclient.api.HttpClientSocket;
import org.webpieces.httpclient.api.ResponseListener;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.nio.api.ChannelManager;

public class HttpClientImpl implements HttpClient {

	private static final Logger log = LoggerFactory.getLogger(HttpClientImpl.class);
	private ChannelManager mgr;
	private HttpParser parser;

	public HttpClientImpl(ChannelManager mgr, HttpParser parser) {
		this.mgr = mgr;
		this.parser = parser;
	}

	@Override
	public CompletableFuture<HttpResponse> sendSingleRequest(InetSocketAddress addr, HttpRequest request) {
		HttpClientSocket socket = openHttpSocket(addr+"");
		CompletableFuture<HttpClientSocket> connect = socket.connect(addr);
		return connect.thenCompose(p -> socket.send(request));
	}
	
	@Override
	public CompletableFuture<HttpChunkWriter> sendSingleRequest(InetSocketAddress addr, HttpRequest request, ResponseListener listener) {
		HttpClientSocket socket = openHttpSocket(addr+"");

		CompletableFuture<HttpClientSocket> connect = socket.connect(addr);
		return connect.thenCompose(p -> socket.send(request, listener));
	}

//	private HttpChunkWriter fail(HttpSocket socket, ResponseListener listener, Throwable e) {
//		CompletableFuture<HttpSocket> closeSocket = socket.closeSocket();
//		closeSocket.exceptionally(ee -> {
//			log.error("could not close socket due to exception");
//			return socket;
//		});
//		listener.failure(e);
//		
//		
//		return null;
//	}

	@Override
	public HttpClientSocket openHttpSocket(String idForLogging) {
		return openHttpSocket(idForLogging, null);
	}
	
	@Override
	public HttpClientSocket openHttpSocket(String idForLogging, CloseListener listener) {
		return new HttpSocketImpl(mgr, idForLogging, null, parser, listener);
	}

}
