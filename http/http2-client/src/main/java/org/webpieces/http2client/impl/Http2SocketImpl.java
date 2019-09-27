package org.webpieces.http2client.impl;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.http2client.api.dto.FullRequest;
import org.webpieces.http2client.api.dto.FullResponse;
import org.webpieces.nio.api.channels.TCPChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.hpack.api.dto.Http2Trailers;
import com.webpieces.http2engine.api.StreamHandle;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2engine.api.client.Http2ClientEngine;
import com.webpieces.http2engine.api.client.Http2ClientEngineFactory;
import com.webpieces.http2parser.api.dto.DataFrame;

public class Http2SocketImpl implements Http2Socket {

	private static final Logger log = LoggerFactory.getLogger(Http2SocketImpl.class);
	private Layer1Incoming incoming;
	private Layer3Outgoing outgoing;

	public Http2SocketImpl(TCPChannel channel, Http2ClientEngineFactory factory) {
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
		SingleResponseListener responseListener = new SingleResponseListener();
		
		StreamHandle streamHandle = openStream();
		
		Http2Request req = request.getHeaders();
		
		if(request.getPayload() == null) {
			request.getHeaders().setEndOfStream(true);
			streamHandle.process(req, responseListener);
			return responseListener.fetchResponseFuture();
		} else if(request.getTrailingHeaders() == null) {
			request.getHeaders().setEndOfStream(false);
			DataFrame data = createData(request, true);
			
			return streamHandle.process(request.getHeaders(), responseListener)
						.thenCompose(writer -> {
							data.setStreamId(req.getStreamId());
							return writer.processPiece(data);
						})
						.thenCompose(writer -> responseListener.fetchResponseFuture());
		}
		
		request.getHeaders().setEndOfStream(false);
		DataFrame data = createData(request, false);
		Http2Trailers trailers = request.getTrailingHeaders();
		trailers.setEndOfStream(true);
		
		return streamHandle.process(request.getHeaders(), responseListener)
				.thenCompose(writer -> writeStuff(writer, req, data, trailers, responseListener));

	}
	
	private CompletableFuture<FullResponse> writeStuff(
			StreamWriter writer, Http2Request req, DataFrame data, Http2Trailers trailers, SingleResponseListener responseListener) {
		
		data.setStreamId(req.getStreamId());
		return writer.processPiece(data)
						.thenCompose(v -> {
							trailers.setStreamId(req.getStreamId());
							return writer.processPiece(trailers);
						})
						.thenCompose(v -> responseListener.fetchResponseFuture());
	}

	private DataFrame createData(FullRequest request, boolean isEndOfStream) {
		DataWrapper payload = request.getPayload();
		DataFrame data = new DataFrame();
		data.setEndOfStream(isEndOfStream);
		data.setData(payload);
		return data;
	}

	@Override
	public StreamHandle openStream() {
		return incoming.openStream();
	}

	@Override
	public CompletableFuture<Void> sendPing() {
		return incoming.sendPing();
	}

}