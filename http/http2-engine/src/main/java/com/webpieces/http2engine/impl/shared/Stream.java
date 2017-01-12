package com.webpieces.http2engine.impl.shared;

import org.webpieces.javasm.api.Memento;

import com.webpieces.http2engine.api.client.Http2ResponseListener;
import com.webpieces.http2engine.api.client.PushPromiseListener;
import com.webpieces.http2parser.api.Http2ParseException;
import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;

public class Stream {

	private Memento currentState;
	private Http2ResponseListener responseListener;
	private PushPromiseListener pushListener;
	private int streamId;
	private long remoteWindowSize;
	private long localWindowSize;
	private long remoteInitialWindowSize;
	private volatile boolean isClosed = false;

	public Stream(
			int streamId, 
			Memento currentState, 
			Http2ResponseListener responseListener, 
			PushPromiseListener pushListener,
			long localInitialWindowSize,
			long remoteInitialWindowSize
	) {
		this.streamId = streamId;
		this.currentState = currentState;
		this.responseListener = responseListener;
		this.pushListener = pushListener;
		localWindowSize = localInitialWindowSize;
		remoteWindowSize = remoteInitialWindowSize;
		
		this.remoteInitialWindowSize = remoteInitialWindowSize;
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

	public long incrementRemoteWindow(long windowSizeIncrement) {
		remoteWindowSize+= windowSizeIncrement;
		if(remoteWindowSize > Integer.MAX_VALUE) {
			throw new Http2ParseException(Http2ErrorCode.FLOW_CONTROL_ERROR, streamId, 
					"(remote end bad)remoteWindowSize too large="+remoteWindowSize+" from windows increment="+windowSizeIncrement+" streamId="+streamId,
					false);
		}
		return remoteWindowSize;
	}

	public long getRemoteWindowSize() {
		return remoteWindowSize;
	}

	public void updateInitialWindow(long initialWindow) {
		long difference = initialWindow - remoteInitialWindowSize;
		remoteInitialWindowSize = initialWindow; //reset this streams initial window size in case it changes again
		remoteWindowSize += difference;

	}

	public long getLocalWindowSize() {
		return localWindowSize;
	}

	public void incrementLocalWindow(long windowSizeIncrement) {
		localWindowSize += windowSizeIncrement;
		if(localWindowSize > Integer.MAX_VALUE) {
			throw new IllegalStateException("Bug, somehow local window got too large");
		}
	}

	public void setIsClosed(boolean b) {
		isClosed = b;
	}

	public boolean isClosed() {
		return isClosed;
	}

	
}
