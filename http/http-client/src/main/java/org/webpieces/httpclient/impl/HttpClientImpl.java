package org.webpieces.httpclient.impl;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import org.webpieces.httpclient.api.*;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import com.webpieces.http2parser.api.Http2Parser;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.nio.api.ChannelManager;

public class HttpClientImpl implements HttpClient {

	private static final Logger log = LoggerFactory.getLogger(HttpClientImpl.class);
	private ChannelManager mgr;
	private HttpParser httpParser;
	private Http2Parser http2Parser;

	public HttpClientImpl(ChannelManager mgr, HttpParser httpParser, Http2Parser http2Parser) {
		this.mgr = mgr;
		this.httpParser = httpParser;
		this.http2Parser = http2Parser;
	}

	@Override
	public CompletableFuture<HttpResponse> sendSingleRequest(InetSocketAddress addr, HttpRequest request) {
		HttpSocket socket = openHttpSocket(addr+"");
		CompletableFuture<RequestSender> connect = socket.connect(addr);
		return connect.thenCompose(requestSender -> requestSender.send(request));
	}
	
	@Override
	public void sendSingleRequest(InetSocketAddress addr, HttpRequest request, ResponseListener listener) {
		HttpSocket socket = openHttpSocket(addr+"");

		CompletableFuture<RequestSender> connect = socket.connect(addr);
		connect.thenAccept(requestSender -> requestSender.sendRequest(request, true, listener))
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
		return new HttpSocketImpl(mgr, idForLogging, null, httpParser, http2Parser, listener);
	}

}
