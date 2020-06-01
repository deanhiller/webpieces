package com.webpieces.http2engine.impl.client;

import org.webpieces.javasm.api.Memento;

import com.webpieces.http2engine.api.PushPromiseListener;
import com.webpieces.http2engine.api.ResponseStreamHandle;
import com.webpieces.http2engine.api.StreamRef;
import com.webpieces.http2engine.impl.shared.data.Stream;

public class ClientStream extends Stream {

	private ResponseStreamHandle responseListener;
	private PushPromiseListener pushListener;
	private StreamRef streamRef;

	public ClientStream(String logId, int streamId, Memento currentState, ResponseStreamHandle responseListener,
			long localInitialWindowSize, long remoteInitialWindowSize) {
		super(logId, streamId, currentState, localInitialWindowSize, remoteInitialWindowSize, true);
		this.responseListener = responseListener;
	}

	public ResponseStreamHandle getResponseListener() {
		return responseListener;
	}
	
	public void setPushListener(PushPromiseListener pushListener) {
		this.pushListener = pushListener;
	}

	public void setResponseStreamRef(StreamRef streamRef) {
		this.streamRef = streamRef;
	}

	public StreamRef getResponseStreamRef() {
		return streamRef;
	}
	
}
