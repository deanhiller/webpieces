package org.webpieces.http2client.impl;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.http2client.api.Http2ServerListener;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.http2client.api.Http2SocketDataWriter;
import org.webpieces.http2client.api.dto.Http2Request;
import org.webpieces.http2client.api.dto.Http2Response;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.api.client.Http2ClientEngine;
import com.webpieces.http2engine.api.client.Http2ClientEngineFactory;
import com.webpieces.http2engine.api.client.Http2Config;
import com.webpieces.http2engine.api.client.Http2ResponseListener;
import com.webpieces.http2parser.api.dto.DataFrame;

public class Http2SocketImpl implements Http2Socket {

	private static final Logger log = LoggerFactory.getLogger(Http2SocketImpl.class);
	private Layer1Incoming incoming;
	private Layer3Outgoing outgoing;

	public Http2SocketImpl(Http2Config config, TCPChannel channel, HpackParser http2Parser, Http2ClientEngineFactory factory) {
		outgoing = new Layer3Outgoing(channel, this);
		Http2ClientEngine parseLayer = factory.createClientParser(config, http2Parser, outgoing);
		incoming = new Layer1Incoming(parseLayer);
	}

	@Override
	public CompletableFuture<Http2Socket> connect(InetSocketAddress addr, Http2ServerListener listener) {
		if(outgoing.getClientListener() != null)
			throw new IllegalStateException("Cannot call connect on an HttpSocket twice");
		else if(listener == null)
			throw new IllegalArgumentException("listener cannot be null");
		else if(addr == null)
			throw new IllegalArgumentException("addr cannot be null");
			
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
			request.getHeaders().setEndOfStream(true);
			sendRequest(request.getHeaders(), responseListener);
			return responseListener.fetchResponseFuture();
		} else if(request.getTrailingHeaders() == null) {
			DataFrame data = createData(request, true);
			
			return sendRequest(request.getHeaders(), responseListener)
						.thenCompose(writer -> writer.sendData(data))
						.thenCompose(writer -> responseListener.fetchResponseFuture());
		}
		
		request.getHeaders().setEndOfStream(false);
		DataFrame data = createData(request, false);
		request.getTrailingHeaders().setEndOfStream(true);
		
		return sendRequest(request.getHeaders(), responseListener)
			.thenCompose(writer -> writer.sendData(data))
			.thenCompose(writer -> writer.sendData(request.getTrailingHeaders()))
			.thenCompose(writer -> responseListener.fetchResponseFuture());
	}

	private DataFrame createData(Http2Request request, boolean isEndOfStream) {
		DataWrapper payload = request.getPayload();
		DataFrame data = new DataFrame();
		data.setEndOfStream(isEndOfStream);
		data.setData(payload);
		return data;
	}

	@Override
	public CompletableFuture<Http2SocketDataWriter> sendRequest(Http2Headers request, Http2ResponseListener listener) {
		return incoming.sendRequest(request, listener);
	}

	@Override
	public CompletableFuture<Void> sendPing() {
		return incoming.sendPing();
	}

}