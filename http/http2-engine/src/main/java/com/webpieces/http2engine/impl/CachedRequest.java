package com.webpieces.http2engine.impl;

import org.webpieces.util.futures.XFuture;

import com.webpieces.http2.api.dto.highlevel.Http2Headers;
import com.webpieces.http2.api.streaming.ResponseStreamHandle;
import com.webpieces.http2.api.streaming.StreamWriter;

public class CachedRequest {

	private Http2Headers frame;
	private ResponseStreamHandle responseListener;
	private XFuture<StreamWriter> future;

	public CachedRequest(Http2Headers frame, ResponseStreamHandle responseListener,
			XFuture<StreamWriter> future) {
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

	public XFuture<StreamWriter> getFuture() {
		return future;
	}
	
}
