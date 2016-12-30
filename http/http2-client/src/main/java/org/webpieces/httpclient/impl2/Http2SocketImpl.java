package org.webpieces.httpclient.impl2;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import org.webpieces.httpclient.api.Http2ResponseListener;
import org.webpieces.httpclient.api.Http2ServerListener;
import org.webpieces.httpclient.api.Http2Socket;
import org.webpieces.httpclient.api.Http2SocketDataWriter;
import org.webpieces.httpclient.api.dto.Http2Headers;
import org.webpieces.httpclient.api.dto.Http2Request;
import org.webpieces.httpclient.api.dto.Http2Response;
import org.webpieces.nio.api.channels.TCPChannel;

import com.webpieces.http2engine.api.Http2EngineFactory;
import com.webpieces.http2engine.api.Http2ClientEngine;
import com.webpieces.http2parser.api.Http2Parser2;

public class Http2SocketImpl implements Http2Socket {

	private Layer1Incoming incoming;
	private Layer5Outgoing outgoing;

	public Http2SocketImpl(TCPChannel channel, Http2Parser2 http2Parser, Http2EngineFactory factory) {
		outgoing = new Layer5Outgoing(channel);
		Http2ClientEngine parseLayer = factory.createClientParser(channel+"", http2Parser, outgoing);
		incoming = new Layer1Incoming(parseLayer, outgoing);
	}

	@Override
	public CompletableFuture<Http2Socket> connect(InetSocketAddress addr, Http2ServerListener listener) {
		if(outgoing.getClientListener() == null)
			outgoing.setClientListener(listener);
		return outgoing.connect(addr, incoming)
				.thenCompose(c -> incoming.sendInitialFrames())  //make sure 'sending' initial frames is part of connecting
				.thenApply(f -> this);
	}

	@Override
	public CompletableFuture<Void> close() {
		//TODO: For http/2, please send GOAWAY first(crap, do we need reason in the close method?...probably)
		return outgoing.close().thenApply(channel -> null);
	}

	@Override
	public CompletableFuture<Http2Response> send(Http2Request request) {
		SingleResponseListener responseListener = new SingleResponseListener();
		
		if(request.getPayload() == null || request.getPayload().getReadableSize() == 0) {
			sendRequest(request.getHeaders(), responseListener, true);
			return responseListener.fetchResponseFuture();
		} else if(request.getTrailingHeaders().getHeaders().size() == 0) {
			return sendRequest(request.getHeaders(), responseListener, false)
						.thenCompose(writer -> writer.sendData(request.getPayload(), true))
						.thenCompose(writer -> responseListener.fetchResponseFuture());
		}
		
		return sendRequest(request.getHeaders(), responseListener, false)
			.thenCompose(writer -> writer.sendData(request.getPayload(), false))
			.thenCompose(writer -> writer.sendTrailingHeaders(request.getTrailingHeaders()))
			.thenCompose(writer -> responseListener.fetchResponseFuture());
	}

	@Override
	public CompletableFuture<Http2SocketDataWriter> sendRequest(Http2Headers request, Http2ResponseListener listener,
			boolean isComplete) {
		return incoming.sendRequest(request, listener, isComplete);
	}

}