package com.webpieces.http2engine.impl;

import java.util.concurrent.CompletableFuture;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2engine.api.client.Http2ResponseListener;

public class CachedRequest {

	private Http2Headers frame;
	private Http2ResponseListener responseListener;
	private CompletableFuture<StreamWriter> future;

	public CachedRequest(Http2Headers frame, Http2ResponseListener responseListener,
			CompletableFuture<StreamWriter> future) {
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

	public CompletableFuture<StreamWriter> getFuture() {
		return future;
	}
	
}
