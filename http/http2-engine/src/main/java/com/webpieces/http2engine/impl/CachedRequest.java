package com.webpieces.http2engine.impl;

import java.util.concurrent.CompletableFuture;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.api.Http2ResponseListener;
import com.webpieces.http2engine.api.RequestWriter;

public class CachedRequest {

	private Http2Headers frame;
	private Http2ResponseListener responseListener;
	private CompletableFuture<RequestWriter> future;

	public CachedRequest(Http2Headers frame, Http2ResponseListener responseListener,
			CompletableFuture<RequestWriter> future) {
				this.frame = frame;
				this.responseListener = responseListener;
				this.future = future;
	}

	public Http2Headers getFrame() {
		return frame;
	}

	public Http2ResponseListener getResponseListener() {
		return responseListener;
	}

	public CompletableFuture<RequestWriter> getFuture() {
		return future;
	}
	
}
