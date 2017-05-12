package org.webpieces.http2client;

import java.util.concurrent.CompletableFuture;

import org.webpieces.http2client.mock.MockResponseListener;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.api.StreamWriter;

public class RequestHolder {

	private Http2Headers request;
	private MockResponseListener listener;
	private CompletableFuture<StreamWriter> future;

	public RequestHolder(Http2Headers request, MockResponseListener listener,
			CompletableFuture<StreamWriter> future) {
				this.request = request;
				this.listener = listener;
				this.future = future;
	}

	public Http2Headers getRequest() {
		return request;
	}

	public MockResponseListener getListener() {
		return listener;
	}

	public CompletableFuture<StreamWriter> getFuture() {
		return future;
	}
	
}
