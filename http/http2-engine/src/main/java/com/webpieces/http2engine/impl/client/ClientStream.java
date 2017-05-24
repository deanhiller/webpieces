package com.webpieces.http2engine.impl.client;

import org.webpieces.javasm.api.Memento;

import com.webpieces.http2engine.api.PushPromiseListener;
import com.webpieces.http2engine.api.ResponseHandler2;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2engine.impl.shared.data.Stream;

public class ClientStream extends Stream {

	private ResponseHandler2 responseListener;
	private StreamWriter responseWriter;
	private PushPromiseListener pushListener;

	public ClientStream(int streamId, Memento currentState, ResponseHandler2 responseListener,
			long localInitialWindowSize, long remoteInitialWindowSize) {
		super(streamId, currentState, localInitialWindowSize, remoteInitialWindowSize);
		this.responseListener = responseListener;
	}

	public ResponseHandler2 getResponseListener() {
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
