package com.webpieces.http2engine.impl.client;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.http2engine.api.ResponseStreamHandle;
import com.webpieces.http2engine.api.RequestStreamHandle;
import com.webpieces.http2engine.api.StreamRef;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2engine.impl.EngineStreamWriter;
import com.webpieces.http2engine.impl.shared.data.Stream;
import com.webpieces.http2parser.api.dto.CancelReason;
import com.webpieces.http2parser.api.dto.RstStreamFrame;

public class ClientStreamHandle implements RequestStreamHandle {

	private AtomicInteger nextAvailableStreamId;
	private Level3ClntOutgoingSyncro outgoingSyncro;
	private boolean requestSent;
	private Stream stream;

	public ClientStreamHandle(AtomicInteger nextAvailableStreamId, Level3ClntOutgoingSyncro outgoingSyncro) {
				this.nextAvailableStreamId = nextAvailableStreamId;
				this.outgoingSyncro = outgoingSyncro;
		
	}

	private int getNextAvailableStreamId() {
		return nextAvailableStreamId.getAndAdd(2);
	}
	
	@Override
	public StreamRef process(Http2Request request, ResponseStreamHandle responseListener) {
		if(request == null)
			throw new IllegalStateException("request cannot be null");
		else if(request.getStreamId() != 0)
			throw new IllegalStateException("Client MUST NOT set Http2Headers.streamId.  that is filled in by library");
		else if(requestSent)
			throw new IllegalStateException("Client can only send ONE request through a stream per http2 spec");
		requestSent = true;
			
		int streamId = getNextAvailableStreamId();
		request.setStreamId(streamId);

		CompletableFuture<StreamWriter> future = outgoingSyncro.sendRequestToSocket(request, responseListener)
				.thenApply(s -> {
					stream = s;
					return new EngineStreamWriter(s, outgoingSyncro);
				});

		return new MyStreamRef(future);
 	}

	public class MyStreamRef implements StreamRef {

		private CompletableFuture<StreamWriter> future;

		public MyStreamRef(CompletableFuture<StreamWriter> future) {
			this.future = future;
		}

		@Override
		public CompletableFuture<StreamWriter> getWriter() {
			return future;
		}

		@Override
		public CompletableFuture<Void> cancel(CancelReason reset) {
			if (stream == null)
				throw new IllegalStateException("You have not sent a request yet so there is nothing to cancel");
			else if (!(reset instanceof RstStreamFrame))
				throw new IllegalArgumentException("This api is re-used on the server to force consistency but client can only pass in RstStreamFrame object to be sent to server here");
			return outgoingSyncro.sendRstToSocket(stream, (RstStreamFrame) reset);
		}
	}
}
