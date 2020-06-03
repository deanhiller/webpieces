package com.webpieces.http2engine.impl.client;

import org.webpieces.javasm.api.Memento;

import com.webpieces.http2.api.streaming.PushPromiseListener;
import com.webpieces.http2.api.streaming.PushStreamHandle;
import com.webpieces.http2.api.streaming.ResponseStreamHandle;
import com.webpieces.http2.api.streaming.StreamWriter;
import com.webpieces.http2engine.impl.shared.data.Stream;

public class ClientPushStream extends Stream {

	private ResponseStreamHandle originalResponseListener;
	private StreamWriter pushResponseWriter;
	private PushPromiseListener pushPromiseListener;
	private PushStreamHandle pushStreamHandle;

	public ClientPushStream(String logId, int streamId, Memento currentState, ResponseStreamHandle responseListener,
			long localInitialWindowSize, long remoteInitialWindowSize) {
		super(logId, streamId, currentState, localInitialWindowSize, remoteInitialWindowSize, false);
		this.originalResponseListener = responseListener;
	}

	public ResponseStreamHandle getOriginalResponseListener() {
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
