package com.webpieces.http2engine.impl;

import java.util.concurrent.CompletableFuture;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.api.ResponseStreamHandle;
import com.webpieces.http2engine.api.StreamWriter;

public class CachedRequest {

	private Http2Headers frame;
	private ResponseStreamHandle responseListener;
	private CompletableFuture<StreamWriter> future;

	public CachedRequest(Http2Headers frame, ResponseStreamHandle responseListener,
			CompletableFuture<StreamWriter> future) {
				this.frame = frame;
				this.responseListener = responseListener;
				this.future = future;
	}

	public Http2Headers getFrame() {
		return frame;
	}

	public ResponseStreamHandle getResponseListener() {
		return responseListener;
	}

	public CompletableFuture<StreamWriter> getFuture() {
		return future;
	}
	
}
