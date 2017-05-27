package com.webpieces.http2engine.impl.client;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.http2engine.api.ResponseHandler2;
import com.webpieces.http2engine.api.StreamHandle;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2engine.impl.EngineStreamWriter;
import com.webpieces.http2engine.impl.shared.data.Stream;
import com.webpieces.http2parser.api.dto.CancelReason;
import com.webpieces.http2parser.api.dto.RstStreamFrame;

public class ClientStreamHandle implements StreamHandle {

	private AtomicInteger nextAvailableStreamId;
	private Level3ClntOutgoingSyncro outgoingSyncro;
	private ResponseHandler2 responseListener;
	private boolean requestSent;
	private Stream stream;

	public ClientStreamHandle(AtomicInteger nextAvailableStreamId, Level3ClntOutgoingSyncro outgoingSyncro,
			ResponseHandler2 responseListener) {
				this.nextAvailableStreamId = nextAvailableStreamId;
				this.outgoingSyncro = outgoingSyncro;
				this.responseListener = responseListener;
		
	}

	private int getNextAvailableStreamId() {
		return nextAvailableStreamId.getAndAdd(2);
	}
	
	@Override
	public CompletableFuture<StreamWriter> process(Http2Request request) {
		if(request.getStreamId() != 0)
			throw new IllegalStateException("Client MUST NOT set Http2Headers.streamId.  that is filled in by library");
		else if(requestSent)
			throw new IllegalStateException("Client can only send ONE request through a stream per http2 spec");
		requestSent = true;
			
		int streamId = getNextAvailableStreamId();
		request.setStreamId(streamId);
		
		return outgoingSyncro.sendRequestToSocket(request, responseListener)
				.thenApply(s -> {
					stream = s;
					return new EngineStreamWriter(s, outgoingSyncro);
				});
	}

	@Override
	public CompletableFuture<Void> cancel(CancelReason reset) {
		if(stream == null)
			throw new IllegalStateException("You have not sent a request yet so there is nothing to cancel");
		else if(!(reset instanceof RstStreamFrame))
			throw new IllegalArgumentException("This api is re-used on the server to force consistency but client can only pass in RstStreamFrame object to be sent to server here");
		return outgoingSyncro.sendRstToSocket(stream, (RstStreamFrame) reset);
	}

}
