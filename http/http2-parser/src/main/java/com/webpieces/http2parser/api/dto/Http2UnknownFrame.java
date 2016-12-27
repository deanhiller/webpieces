package com.webpieces.http2parser.api.dto;

import org.webpieces.data.api.DataWrapper;

public class Http2UnknownFrame extends Http2Frame {

	private byte flagsByte;
	private byte frameTypeId;
	private int streamId;
	private DataWrapper framePayloadData;

	public Http2UnknownFrame(byte flagsByte, byte frameTypeId, int streamId, DataWrapper framePayloadData) {
		this.flagsByte = flagsByte;
		this.frameTypeId = frameTypeId;
		this.streamId = streamId;
		this.framePayloadData = framePayloadData;
	}

	@Override
	public Http2FrameType getFrameType() {
		throw new UnsupportedOperationException("not supported yet");
	}
	
	public byte getFlagsByte() {
		return flagsByte;
	}

	public byte getFrameTypeId() {
		return frameTypeId;
	}

	public int getStreamId() {
		return streamId;
	}

	public DataWrapper getFramePayloadData() {
		return framePayloadData;
	}

	@Override
	public String toString() {
		return "Http2UnknownFrame [flagsByte=" + flagsByte + ", frameTypeId=" + frameTypeId + ", streamId=" + streamId
				+ ", framePayloadData=" + framePayloadData + "]";
	}
	
}
