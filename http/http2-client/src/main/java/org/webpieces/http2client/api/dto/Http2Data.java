package org.webpieces.http2client.api.dto;

import org.webpieces.data.api.DataWrapper;

public class Http2Data implements PartialResponse {

	private int streamId;
	private boolean lastPartOfResponse = false;
	private DataWrapper payload;

	public int getStreamId() {
		return streamId;
	}

	public void setStreamId(int streamId) {
		this.streamId = streamId;
	}

	public boolean isLastPartOfResponse() {
		return lastPartOfResponse;
	}

	public void setLastPartOfResponse(boolean lastPartOfResponse) {
		this.lastPartOfResponse = lastPartOfResponse;
	}

	public DataWrapper getPayload() {
		return payload;
	}

	public void setPayload(DataWrapper payload) {
		this.payload = payload;
	}

	@Override
	public String toString() {
		return "Http2Data [streamId=" + streamId + ", lastPartOfResponse=" + lastPartOfResponse + ", payload=" + payload.getReadableSize()
				+ "]";
	}
}
