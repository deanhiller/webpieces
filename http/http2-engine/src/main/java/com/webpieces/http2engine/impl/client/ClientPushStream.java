package com.webpieces.http2engine.impl.client;

import org.webpieces.javasm.api.Memento;

import com.webpieces.http2engine.api.PushPromiseListener;
import com.webpieces.http2engine.api.PushStreamHandle;
import com.webpieces.http2engine.api.ResponseHandler2;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2engine.impl.shared.data.Stream;

public class ClientPushStream extends Stream {

	private ResponseHandler2 originalResponseListener;
	private StreamWriter pushResponseWriter;
	private PushPromiseListener pushPromiseListener;
	private PushStreamHandle pushStreamHandle;

	public ClientPushStream(int streamId, Memento currentState, ResponseHandler2 responseListener,
			long localInitialWindowSize, long remoteInitialWindowSize) {
		super(streamId, currentState, localInitialWindowSize, remoteInitialWindowSize);
		this.originalResponseListener = responseListener;
	}

	public ResponseHandler2 getOriginalResponseListener() {
		return originalResponseListener;
	}
	
	public void setPushResponseWriter(StreamWriter w) {
		pushResponseWriter = w;
	}
	
	public StreamWriter getPushResponseWriter() {
		return pushResponseWriter;
	}

	public void setPushPromiseListener(PushPromiseListener l) {
		this.pushPromiseListener = l;
	}

	public void setPushStreamHandle(PushStreamHandle pushHandle) {
		this.pushStreamHandle = pushHandle;
	}

	public PushPromiseListener getPushPromiseListener() {
		return pushPromiseListener;
	}

	public PushStreamHandle getPushStreamHandle() {
		return pushStreamHandle;
	}
	
}
