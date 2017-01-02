package com.webpieces.http2engine.api;

import org.webpieces.data.api.DataWrapper;

public class Http2Data implements PartialStream {

	private int streamId;
	private boolean endOfStream = false;
	private DataWrapper payload;

	public Http2Data() {}
	
	public Http2Data(int streamId, boolean endOfStream, DataWrapper payload) {
		super();
		this.streamId = streamId;
		this.endOfStream = endOfStream;
		this.payload = payload;
	}

	public int getStreamId() {
		return streamId;
	}

	public void setStreamId(int streamId) {
		this.streamId = streamId;
	}

	public boolean isEndOfStream() {
		return endOfStream;
	}

	public void setEndOfStream(boolean lastPartOfResponse) {
		this.endOfStream = lastPartOfResponse;
	}

	public DataWrapper getPayload() {
		return payload;
	}

	public void setPayload(DataWrapper payload) {
		this.payload = payload;
	}

	@Override
	public String toString() {
		return "Http2Data [streamId=" + streamId + ", lastPartOfResponse=" + endOfStream + ", payload=" + payload.getReadableSize()
				+ "]";
	}
}
