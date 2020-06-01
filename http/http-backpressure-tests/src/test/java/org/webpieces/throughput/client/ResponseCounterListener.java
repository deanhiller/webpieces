package org.webpieces.throughput.client;

import java.util.concurrent.CompletableFuture;

import org.webpieces.util.time.RateRecorder;

import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.PushStreamHandle;
import com.webpieces.http2engine.api.ResponseStreamHandle;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.CancelReason;

public class ResponseCounterListener implements ResponseStreamHandle {

	private RateRecorder recorder = new RateRecorder(10);

	@Override
	public CompletableFuture<StreamWriter> process(Http2Response response) {
		
		recorder.increment();

		return CompletableFuture.completedFuture(null);
	}

	@Override
	public PushStreamHandle openPushStream() {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public CompletableFuture<Void> cancel(CancelReason payload) {
		throw new UnsupportedOperationException("not implemented");
	}

}
