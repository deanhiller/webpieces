package org.webpieces.httpclient.impl2;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import org.webpieces.httpclient.api.Http2ResponseListener;
import org.webpieces.httpclient.api.Http2ServerListener;
import org.webpieces.httpclient.api.Http2Socket;
import org.webpieces.httpclient.api.Http2SocketDataWriter;
import org.webpieces.httpclient.api.dto.Http2Request;
import org.webpieces.httpclient.api.dto.Http2Response;
import org.webpieces.nio.api.channels.TCPChannel;

import com.webpieces.http2parser.api.Http2Parser;

public class Http2SocketImpl implements Http2Socket {

	private Http2SocketDataListener socketDataListener;
	private TCPChannel channel;
	private Http2Parser http2Parser;

	public Http2SocketImpl(TCPChannel channel, Http2Parser http2Parser) {
		this.channel = channel;
		this.http2Parser = http2Parser;
	}

	@Override
	public CompletableFuture<Http2Socket> connect(InetSocketAddress addr, Http2ServerListener listener) {
		if(socketDataListener != null)
			throw new IllegalStateException("This socket was already connected and can no longer be re-used");
		
		socketDataListener = new Http2SocketDataListener(this, listener);
		channel.connect(addr, socketDataListener);
		
		return null;
	}

	@Override
	public CompletableFuture<Void> close() {
		//TODO: For http/2, please send GOAWAY first(crap, do we need reason in the close method?...probably)
		return channel.close().thenApply(channel -> null);
	}

	@Override
	public CompletableFuture<Http2Response> send(Http2Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Http2SocketDataWriter> sendRequest(Http2Request request, Http2ResponseListener listener,
			boolean isComplete) {
		// TODO Auto-generated method stub
		return null;
	}



}
