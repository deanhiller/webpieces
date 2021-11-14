package org.webpieces.throughput.client;

import org.webpieces.util.futures.XFuture;

import org.webpieces.util.time.RateRecorder;

import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.streaming.PushStreamHandle;
import com.webpieces.http2.api.streaming.ResponseStreamHandle;
import com.webpieces.http2.api.streaming.StreamWriter;

public class ResponseCounterListener implements ResponseStreamHandle {

	private RateRecorder recorder = new RateRecorder(10);

	@Override
	public XFuture<StreamWriter> process(Http2Response response) {
		
		recorder.increment();

		return XFuture.completedFuture(null);
	}

	@Override
	public PushStreamHandle openPushStream() {
		throw new UnsupportedOperationException("not implemented");
	}
	
	@Override
	public XFuture<Void> cancel(CancelReason payload) {
		throw new UnsupportedOperationException("not implemented");
	}

}
