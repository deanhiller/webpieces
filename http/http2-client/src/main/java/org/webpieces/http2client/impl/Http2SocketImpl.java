package org.webpieces.http2client.impl;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.http2client.api.dto.FullRequest;
import org.webpieces.http2client.api.dto.FullResponse;
import org.webpieces.nio.api.channels.TCPChannel;

import com.webpieces.http2.api.streaming.RequestStreamHandle;
import com.webpieces.http2engine.api.client.Http2ClientEngine;
import com.webpieces.http2engine.api.client.Http2ClientEngineFactory;

public class Http2SocketImpl implements Http2Socket {

	private static final Logger log = LoggerFactory.getLogger(Http2SocketImpl.class);
	private Layer1Incoming incoming;
	private Layer3Outgoing outgoing;

	public Http2SocketImpl(Http2ChannelProxy channel, Http2ClientEngineFactory factory) {
		outgoing = new Layer3Outgoing(channel, this);
		
		Http2ClientEngine parseLayer = factory.createClientParser(""+channel, outgoing);
		incoming = new Layer1Incoming(parseLayer);
	}

	@Override
	public CompletableFuture<Void> connect(InetSocketAddress addr) {
		if(addr == null)
			throw new IllegalArgumentException("addr cannot be null");
			
		return outgoing.connect(addr, incoming)
				.thenCompose(c -> incoming.sendInitialFrames())  //make sure 'sending' initial frames is part of connecting
				.thenApply(f -> {
					log.info("connecting complete as initial frames sent");
					return null;
				});
	}

	@Override
	public CompletableFuture<Void> close() {
		//TODO: For http/2, please send GOAWAY first(crap, do we need reason in the close method?...probably)
		return outgoing.close();
	}

	/**
	 * Can't specifically backpressure with this method(ie. On the other method, if you do not ack, eventually
	 * with too many bytes, the channelmanager disregisters and stops reading from the socket placing backpressure
	 * on the socket)
	 */
	@Override
	public CompletableFuture<FullResponse> send(FullRequest request) {
		return new ResponseCacher(() -> openStream()).run(request);
	}

	@Override
	public RequestStreamHandle openStream() {
		return incoming.openStream();
	}

	@Override
	public CompletableFuture<Void> sendPing() {
		return incoming.sendPing();
	}

}