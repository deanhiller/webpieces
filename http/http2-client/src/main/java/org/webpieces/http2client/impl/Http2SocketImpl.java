package org.webpieces.http2client.impl;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.http2client.api.dto.FullRequest;
import org.webpieces.http2client.api.dto.FullResponse;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.hpack.api.dto.Http2Trailers;
import com.webpieces.http2engine.api.ResponseHandler2;
import com.webpieces.http2engine.api.StreamHandle;
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
	public CompletableFuture<Http2Socket> connect(InetSocketAddress addr) {
		if(addr == null)
			throw new IllegalArgumentException("addr cannot be null");
			
		return outgoing.connect(addr, incoming)
				.thenCompose(c -> incoming.sendInitialFrames())  //make sure 'sending' initial frames is part of connecting
				.thenApply(f -> {
					log.info("connecting complete as initial frames sent");
					return this;
				});
	}

	@Override
	public CompletableFuture<Http2Socket> close() {
		//TODO: For http/2, please send GOAWAY first(crap, do we need reason in the close method?...probably)
		return outgoing.close().thenApply(channel -> this);
	}

	/**
	 * Can't specifically backpressure with this method(ie. On the other method, if you do not ack, eventually
	 * with too many bytes, the channelmanager disregisters and stops reading from the socket placing backpressure
	 * on the socket)
	 */
	@Override
	public CompletableFuture<FullResponse> send(FullRequest request) {
		SingleResponseListener responseListener = new SingleResponseListener();
		
		StreamHandle streamHandle = openStream(responseListener);
		
		Http2Request req = request.getHeaders();
		
		if(request.getPayload() == null) {
			request.getHeaders().setEndOfStream(true);
			streamHandle.process(req);
			return responseListener.fetchResponseFuture();
		} else if(request.getTrailingHeaders() == null) {
			request.getHeaders().setEndOfStream(false);
			DataFrame data = createData(request, true);
			
			return streamHandle.process(request.getHeaders())
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
		
		return streamHandle.process(request.getHeaders())
				.thenCompose(writer -> {
					data.setStreamId(req.getStreamId());
					return writer.processPiece(data);
				})
			.thenCompose(writer -> {
				trailers.setStreamId(req.getStreamId());
				return writer.processPiece(trailers);
			})
			.thenCompose(writer -> responseListener.fetchResponseFuture());
	}

	private DataFrame createData(FullRequest request, boolean isEndOfStream) {
		DataWrapper payload = request.getPayload();
		DataFrame data = new DataFrame();
		data.setEndOfStream(isEndOfStream);
		data.setData(payload);
		return data;
	}

	@Override
	public StreamHandle openStream(ResponseHandler2 listener) {
		return incoming.openStream(listener);
	}

	@Override
	public CompletableFuture<Void> sendPing() {
		return incoming.sendPing();
	}

}