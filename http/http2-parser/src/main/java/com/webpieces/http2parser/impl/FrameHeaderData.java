package com.webpieces.http2parser.impl;

public class FrameHeaderData {

	private int payloadLength;
	private int streamId;
	private byte frameTypeId;
	private byte flagsByte;

	public FrameHeaderData(int payloadLength, int streamId, byte frameTypeId, byte flagsByte) {
		this.payloadLength = payloadLength;
		this.streamId = streamId;
		this.frameTypeId = frameTypeId;
		this.flagsByte = flagsByte;
	}

	public int getPayloadLength() {
		return payloadLength;
	}

	public int getStreamId() {
		return streamId;
	}

	public byte getFrameTypeId() {
		return frameTypeId;
	}

	public byte getFlagsByte() {
		return flagsByte;
	}
	
}
