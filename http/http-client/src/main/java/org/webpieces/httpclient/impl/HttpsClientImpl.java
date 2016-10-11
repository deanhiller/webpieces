package org.webpieces.httpclient.impl;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import com.webpieces.http2parser.api.Http2Parser;
import org.webpieces.httpclient.api.*;
import org.webpieces.httpcommon.api.CloseListener;
import org.webpieces.httpcommon.api.RequestSender;
import org.webpieces.httpcommon.api.ResponseListener;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.nio.api.ChannelManager;

public class HttpsClientImpl implements HttpClient {

	private static final Logger log = LoggerFactory.getLogger(HttpClientImpl.class);
	private ChannelManager mgr;
	private HttpParser httpParser;
	private Http2Parser http2Parser;
	private HttpsSslEngineFactory factory;

	public HttpsClientImpl(ChannelManager mgr, HttpParser httpParser, Http2Parser http2Parser, HttpsSslEngineFactory factory) {
		this.mgr = mgr;
		this.httpParser = httpParser;
		this.http2Parser = http2Parser;
		this.factory = factory;
	}

	@Override
	public CompletableFuture<HttpResponse> sendSingleRequest(InetSocketAddress addr, HttpRequest request) {
		HttpClientSocket socket = openHttpSocket(addr+"");
		CompletableFuture<RequestSender> connect = socket.connect(addr);
		return connect.thenCompose(requestSender -> requestSender.send(request));
	}

	@Override
	public void sendSingleRequest(InetSocketAddress addr, HttpRequest request, ResponseListener listener) {
		HttpClientSocket socket = openHttpSocket(addr+"");

		CompletableFuture<RequestSender> connect = socket.connect(addr);
		connect.thenAccept(requestListener -> requestListener.sendRequest(request, true, listener))
			.exceptionally(e -> fail(socket, listener, e));
	}

	private Void fail(HttpClientSocket socket, ResponseListener listener, Throwable e) {
		CompletableFuture<Void> closeSocket = socket.closeSocket();
		closeSocket.exceptionally(ee -> {
			log.error("could not close socket due to exception");
			return null;
		});
		listener.failure(e);
		return null;
	}

	@Override
	public HttpClientSocket openHttpSocket(String idForLogging) {
		return openHttpSocket(idForLogging, null);
	}

	@Override
	public HttpClientSocket openHttpSocket(String idForLogging, CloseListener listener) {
		return new HttpClientSocketImpl(mgr, idForLogging, factory, httpParser, http2Parser, listener);
	}


}
