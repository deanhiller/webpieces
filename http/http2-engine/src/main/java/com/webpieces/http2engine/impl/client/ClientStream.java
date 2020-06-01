package com.webpieces.http2engine.impl.client;

import java.util.concurrent.CompletableFuture;

import org.webpieces.javasm.api.Memento;

import com.webpieces.http2engine.api.PushPromiseListener;
import com.webpieces.http2engine.api.ResponseStreamHandle;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2engine.impl.shared.data.Stream;

public class ClientStream extends Stream {

	private ResponseStreamHandle responseListener;
	private CompletableFuture<StreamWriter> responseWriter = new CompletableFuture<StreamWriter>();
	private PushPromiseListener pushListener;

	public ClientStream(String logId, int streamId, Memento currentState, ResponseStreamHandle responseListener,
			long localInitialWindowSize, long remoteInitialWindowSize) {
		super(logId, streamId, currentState, localInitialWindowSize, remoteInitialWindowSize, true);
		this.responseListener = responseListener;
	}

	public ResponseStreamHandle getResponseListener() {
		return responseListener;
	}
	
	public void setResponseWriter(StreamWriter w) {
		responseWriter.complete(w);
	}
	
	public void setPushListener(PushPromiseListener pushListener) {
		this.pushListener = pushListener;
	}

	public CompletableFuture<StreamWriter> getResponseWriter() {
		return responseWriter;
	}

}
