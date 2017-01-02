package com.webpieces.http2engine.impl;

import org.webpieces.javasm.api.Memento;

import com.webpieces.http2engine.api.Http2ResponseListener;
import com.webpieces.http2engine.api.PushPromiseListener;

public class Stream {

	private Memento currentState;
	private Http2ResponseListener responseListener;
	private PushPromiseListener pushListener;
	private int streamId;

	public Stream(int streamId, Memento currentState, Http2ResponseListener responseListener, PushPromiseListener pushListener) {
		this.streamId = streamId;
		this.currentState = currentState;
		this.responseListener = responseListener;
		this.pushListener = pushListener;
	}

	public int getStreamId() {
		return streamId;
	}

	public Memento getCurrentState() {
		return currentState;
	}

	public Http2ResponseListener getResponseListener() {
		return responseListener;
	}

	public PushPromiseListener getPushListener() {
		return pushListener;
	}

}
