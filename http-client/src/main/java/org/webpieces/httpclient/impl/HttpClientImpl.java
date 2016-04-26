package org.webpieces.httpclient.impl;

import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;

import org.webpieces.httpclient.api.HttpClient;
import org.webpieces.httpclient.api.HttpSocket;
import org.webpieces.httpclient.api.Response;
import org.webpieces.nio.api.BufferCreationPool;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.channels.TCPChannel;

import com.webpieces.httpparser.api.HttpParser;
import com.webpieces.httpparser.api.dto.HttpRequest;
import com.webpieces.httpparser.api.dto.HttpResponse;

public class HttpClientImpl implements HttpClient {

	private ChannelManager mgr;
	private HttpParser parser;
	private BufferCreationPool pool;

	public HttpClientImpl(ChannelManager mgr, HttpParser parser, BufferCreationPool pool) {
		this.mgr = mgr;
		this.parser = parser;
		this.pool = pool;
	}

	@Override
	public CompletableFuture<HttpResponse> sendSingleRequest(HttpRequest request) {
		CompletableFuture<HttpResponse> promise = new CompletableFuture<>();
//		SocketAddress addr = figureOutSocket(request);
//		HttpSocket socket = openHttpSocket(addr);
//		Future<Response> future = socket.send(request);
//		future.chain(promise, p-> adapt(p));
		return promise;
	}

	private HttpResponse adapt(Response resp) {
		return resp.getResponse();
	}
	
	private SocketAddress figureOutSocket(HttpRequest request) {
		return null;
	}

	@Override
	public HttpSocket openHttpSocket(SocketAddress addr) {
		TCPChannel channel = mgr.createTCPChannel(""+addr);
		return new HttpSocketImpl(channel, parser, addr);
	}

}
