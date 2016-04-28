package org.webpieces.httpclient.impl;

import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.httpclient.api.HttpClient;
import org.webpieces.httpclient.api.HttpSocket;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.channels.TCPChannel;

import com.webpieces.httpparser.api.HttpParser;
import com.webpieces.httpparser.api.dto.HttpRequest;
import com.webpieces.httpparser.api.dto.HttpResponse;

public class HttpClientImpl implements HttpClient {

	private static final Logger log = LoggerFactory.getLogger(HttpClientImpl.class);
	private ChannelManager mgr;
	private HttpParser parser;

	public HttpClientImpl(ChannelManager mgr, HttpParser parser) {
		this.mgr = mgr;
		this.parser = parser;
	}

	@Override
	public CompletableFuture<HttpResponse> sendSingleRequest(HttpRequest request) {
		SocketAddress addr = figureOutSocket(request);
		HttpSocket socket = openHttpSocket(request.getRequestLine()+"");

		return socket.connect(addr)
			.thenCompose(c -> socket.send(request))
			.thenCompose(r -> closeSocket(r, socket));
	}

	private CompletableFuture<HttpResponse> closeSocket(HttpResponse response, HttpSocket socket) {
		return socket.closeSocket()
				.exceptionally(e -> {
					//log close exception and close normally...
					log.info("Exception closing socket after response", e);
					return socket;
				})
				.thenApply(s -> response);
	}
	
	private SocketAddress figureOutSocket(HttpRequest request) {
		throw new UnsupportedOperationException("not supported yet");
	}

	@Override
	public HttpSocket openHttpSocket(String idForLogging) {
		TCPChannel channel = mgr.createTCPChannel(idForLogging);
		return new HttpSocketImpl(channel, parser);
	}

}
