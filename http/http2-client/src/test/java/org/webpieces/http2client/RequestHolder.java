package org.webpieces.http2client;

import java.util.concurrent.CompletableFuture;

import org.webpieces.http2client.mock.MockResponseListener;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.api.client.ClientStreamWriter;

public class RequestHolder {

	private Http2Headers request;
	private MockResponseListener listener;
	private CompletableFuture<ClientStreamWriter> future;

	public RequestHolder(Http2Headers request, MockResponseListener listener,
			CompletableFuture<ClientStreamWriter> future) {
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

	public CompletableFuture<ClientStreamWriter> getFuture() {
		return future;
	}
	
}
