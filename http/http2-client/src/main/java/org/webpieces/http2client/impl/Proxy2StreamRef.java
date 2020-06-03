package org.webpieces.http2client.impl;

import java.util.concurrent.CompletableFuture;

import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.streaming.StreamRef;
import com.webpieces.http2.api.streaming.StreamWriter;

public class Proxy2StreamRef implements StreamRef {

	private StreamRef ref;
	private CompletableFuture<StreamWriter> writer;

	public Proxy2StreamRef(StreamRef ref, CompletableFuture<StreamWriter> writer) {
		this.ref = ref;
		this.writer = writer;
	}

	@Override
	public CompletableFuture<StreamWriter> getWriter() {
		return writer;
	}

	@Override
	public CompletableFuture<Void> cancel(CancelReason reason) {
		if(ref != null)
			return ref.cancel(reason);
		
		return CompletableFuture.completedFuture(null);
	}

}
