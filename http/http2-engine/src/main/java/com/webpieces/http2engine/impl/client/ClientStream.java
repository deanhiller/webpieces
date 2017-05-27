package com.webpieces.http2engine.impl.client;

import org.webpieces.javasm.api.Memento;

import com.webpieces.http2engine.api.PushPromiseListener;
import com.webpieces.http2engine.api.ResponseHandler;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2engine.impl.shared.data.Stream;

public class ClientStream extends Stream {

	private ResponseHandler responseListener;
	private StreamWriter responseWriter;
	private PushPromiseListener pushListener;

	public ClientStream(String logId, int streamId, Memento currentState, ResponseHandler responseListener,
			long localInitialWindowSize, long remoteInitialWindowSize) {
		super(logId, streamId, currentState, localInitialWindowSize, remoteInitialWindowSize, true);
		this.responseListener = responseListener;
	}

	public ResponseHandler getResponseListener() {
		return responseListener;
	}
	
	public void setResponseWriter(StreamWriter w) {
		responseWriter = w;
	}
	
	public void setPushListener(PushPromiseListener pushListener) {
		this.pushListener = pushListener;
	}

	public StreamWriter getResponseWriter() {
		return responseWriter;
	}

}
