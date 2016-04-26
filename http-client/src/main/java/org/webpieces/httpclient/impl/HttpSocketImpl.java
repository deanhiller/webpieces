package org.webpieces.httpclient.impl;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;

import org.webpieces.httpclient.api.HttpSocket;
import org.webpieces.httpclient.api.Response;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;

import com.webpieces.httpparser.api.HttpParser;
import com.webpieces.httpparser.api.dto.HttpRequest;

public class HttpSocketImpl implements HttpSocket, Closeable {

	private TCPChannel channel;
	private HttpParser parser;
	private SocketAddress addr;

	public HttpSocketImpl(TCPChannel channel, HttpParser parser, SocketAddress addr) {
		this.channel = channel;
		this.parser = parser;
		this.addr = addr;
	}

	@Override
	public CompletableFuture<Response> send(HttpRequest request) {
		CompletableFuture<Channel> f = null;
		
//		f.acceptEither(other, action)
//		Future<Channel> connectFuture = channel.connect(addr);
//		connectFuture.
//		
//		
//		byte[] bytes = parser.marshalToBytes(request);
//		ByteBuffer wrap = ByteBuffer.wrap(bytes);
//		Future<Channel> write = channel.write(wrap);
		
		
		return null;
	}
	
	@Override
	public void close() throws IOException {
		closeSocket();
	}
	
	@Override
	public CompletableFuture<HttpSocket> closeSocket() {
		CompletableFuture<HttpSocket> promise = new CompletableFuture<>();
//		Future<Channel> future = channel.close();
//		future.chain(promise, p -> adapt(p));
		return promise;
	}
	
	private HttpSocket adapt(Channel c) {
		return this;
	}

}
