package org.webpieces.http2client.impl;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import org.webpieces.http2client.api.Http2ResponseListener;
import org.webpieces.http2client.api.Http2ServerListener;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.http2client.api.Http2SocketDataWriter;
import org.webpieces.http2client.api.dto.Http2Request;
import org.webpieces.http2client.api.dto.Http2Response;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.http2engine.api.Http2EngineFactory;
import com.webpieces.http2engine.api.dto.Http2Headers;
import com.webpieces.http2engine.api.Http2ClientEngine;
import com.webpieces.http2parser.api.Http2Parser2;

public class Http2SocketImpl implements Http2Socket {

	private static final Logger log = LoggerFactory.getLogger(Http2SocketImpl.class);
	private Layer1Incoming incoming;
	private Layer5Outgoing outgoing;

	public Http2SocketImpl(TCPChannel channel, Http2Parser2 http2Parser, Http2EngineFactory factory) {
		outgoing = new Layer5Outgoing(channel, this);
		Http2ClientEngine parseLayer = factory.createClientParser(channel+"", http2Parser, outgoing);
		incoming = new Layer1Incoming(parseLayer, outgoing);
	}

	@Override
	public CompletableFuture<Http2Socket> connect(InetSocketAddress addr, Http2ServerListener listener) {
		if(outgoing.getClientListener() == null)
			outgoing.setClientListener(listener);
		return outgoing.connect(addr, incoming)
				.thenCompose(c -> incoming.sendInitialFrames())  //make sure 'sending' initial frames is part of connecting
				.thenApply(f -> {
					log.info("connecting complete as initial frames sent");
					return this;
				});
	}

	@Override
	public CompletableFuture<Void> close() {
		//TODO: For http/2, please send GOAWAY first(crap, do we need reason in the close method?...probably)
		return outgoing.close().thenApply(channel -> null);
	}

	@Override
	public CompletableFuture<Http2Response> send(Http2Request request) {
		SingleResponseListener responseListener = new SingleResponseListener();
		
		if(request.getPayload() == null) {
			sendRequest(request.getHeaders(), responseListener, true);
			return responseListener.fetchResponseFuture();
		} else if(request.getTrailingHeaders() == null) {
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