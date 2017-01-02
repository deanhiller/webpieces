package com.webpieces.http2engine.api;

import org.webpieces.data.api.DataWrapper;

public class Http2UnknownFrame implements PartialStream {

	private int streamId;
	private byte flagsByte;
	private byte frameTypeId;
	private DataWrapper framePayloadData;
	
	@Override
	public boolean isEndOfStream() {
		return false;
	}

	@Override
	public int getStreamId() {
		return streamId;
	}

	public void setStreamId(int streamId) {
		this.streamId = streamId;
	}

	public byte getFlagsByte() {
		return flagsByte;
	}

	public void setFlagsByte(byte flagsByte) {
		this.flagsByte = flagsByte;
	}

	public byte getFrameTypeId() {
		return frameTypeId;
	}

	public void setFrameTypeId(byte frameTypeId) {
		this.frameTypeId = frameTypeId;
	}

	public DataWrapper getFramePayloadData() {
		return framePayloadData;
	}

	public void setFramePayloadData(DataWrapper framePayloadData) {
		this.framePayloadData = framePayloadData;
	}

	@Override
	public String toString() {
		return "Http2UnknownFrame [streamId=" + streamId + ", flagsByte=" + flagsByte + ", frameTypeId=" + frameTypeId
				+ ", framePayloadData=" + framePayloadData.getReadableSize() + "]";
	}

}
