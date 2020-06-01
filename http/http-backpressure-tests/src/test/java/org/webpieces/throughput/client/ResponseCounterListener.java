package org.webpieces.throughput.client;

import java.util.concurrent.CompletableFuture;

import org.webpieces.util.time.RateRecorder;

import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.PushStreamHandle;
import com.webpieces.http2engine.api.ResponseStreamHandle;
import com.webpieces.http2engine.api.StreamRef;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.CancelReason;

public class ResponseCounterListener implements ResponseStreamHandle {

	private RateRecorder recorder = new RateRecorder(10);

	@Override
	public StreamRef process(Http2Response response) {
		
		recorder.increment();

		CompletableFuture<StreamWriter> writer = CompletableFuture.completedFuture(null);
		return new MyStreamRef(writer);
	}

	private class MyStreamRef implements StreamRef {

		private CompletableFuture<StreamWriter> writer;

		public MyStreamRef(CompletableFuture<StreamWriter> writer) {
			this.writer = writer;
		}

		@Override
		public CompletableFuture<StreamWriter> getWriter() {
			return writer;
		}

		@Override
		public CompletableFuture<Void> cancel(CancelReason reason) {
			throw new UnsupportedOperationException("not implemented");
		}
	}
	
	@Override
	public PushStreamHandle openPushStream() {
		throw new UnsupportedOperationException("not implemented");
	}

}
