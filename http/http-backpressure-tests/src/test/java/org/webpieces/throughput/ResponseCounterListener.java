package org.webpieces.throughput;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import org.webpieces.util.time.MsgRateRecorder;

import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.PushStreamHandle;
import com.webpieces.http2engine.api.ResponseHandler;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.CancelReason;

public class ResponseCounterListener implements ResponseHandler {

	private MsgRateRecorder recorder = new MsgRateRecorder(10);

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
