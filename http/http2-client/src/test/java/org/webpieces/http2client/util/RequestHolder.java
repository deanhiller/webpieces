package org.webpieces.http2client.util;

import java.util.concurrent.CompletableFuture;

import org.webpieces.http2client.mock.MockResponseListener;
import org.webpieces.http2client.mock.MockStreamWriter;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.http2engine.api.StreamWriter;

public class RequestHolder {

	private Http2Request request;
	private MockResponseListener listener;
	private CompletableFuture<StreamWriter> future;
	private MockStreamWriter writer;

	public RequestHolder(Http2Request request, MockResponseListener listener,
			MockStreamWriter writer, CompletableFuture<StreamWriter> future) {
				this.request = request;
				this.listener = listener;
				this.writer = writer;
				this.future = future;
	}

	public Http2Request getRequest() {
		return request;
	}

	public MockResponseListener getListener() {
		return listener;
	}

	public CompletableFuture<StreamWriter> getFuture() {
		return future;
	}

	public MockStreamWriter getWriter() {
		return writer;
	}
	
}
