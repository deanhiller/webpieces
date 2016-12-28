package org.webpieces.httpclient.impl2;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import org.webpieces.httpclient.api2.HttpResponseListener;
import org.webpieces.httpclient.api2.HttpServerListener;
import org.webpieces.httpclient.api2.HttpSocket;
import org.webpieces.httpclient.api2.HttpSocketDataWriter;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.nio.api.channels.TCPChannel;

import com.webpieces.http2parser.api.Http2Parser;

public class HttpSocketImpl implements HttpSocket {

	private SocketDataListener socketDataListener;
	private TCPChannel channel;
	private HttpParser httpParser;
	private Http2Parser http2Parser;
	private boolean forceHttp2; 

	public HttpSocketImpl(TCPChannel channel, HttpParser httpParser, Http2Parser http2Parser, boolean forceHttp2) {
		this.channel = channel;
		this.httpParser = httpParser;
		this.http2Parser = http2Parser;
		this.forceHttp2 = forceHttp2;
	}

	@Override
	public CompletableFuture<HttpSocket> connect(InetSocketAddress addr, HttpServerListener listener) {
		if(socketDataListener != null)
			throw new IllegalStateException("This socket was already connected and can no longer be re-used");
		
		socketDataListener = new SocketDataListener(this, listener);
		channel.connect(addr, socketDataListener);
		
		return null;
	}
	
	@Override
	public CompletableFuture<HttpResponse> send(HttpRequest request) {
		return null;
	}

	@Override
	public CompletableFuture<HttpSocketDataWriter> sendRequest(HttpRequest request, HttpResponseListener listener,
			boolean isComplete) {
		return null;
	}

	@Override
	public CompletableFuture<Void> close() {
		//TODO: For http/2, please send GOAWAY first(crap, do we need reason in the close method?...probably)
		return channel.close().thenApply(channel -> null);
	}

}
