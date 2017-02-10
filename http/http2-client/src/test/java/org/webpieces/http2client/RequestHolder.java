package org.webpieces.http2client;

import java.util.concurrent.CompletableFuture;

import org.webpieces.http2client.api.Http2SocketDataWriter;
import org.webpieces.http2client.mock.MockResponseListener;

import com.webpieces.hpack.api.dto.Http2Headers;

public class RequestHolder {

	private Http2Headers request;
	private MockResponseListener listener;
	private CompletableFuture<Http2SocketDataWriter> future;

	public RequestHolder(Http2Headers request, MockResponseListener listener,
			CompletableFuture<Http2SocketDataWriter> future) {
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

	public CompletableFuture<Http2SocketDataWriter> getFuture() {
		return future;
	}
	
}
