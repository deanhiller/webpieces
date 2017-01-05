package com.webpieces.http2parser.api.dto;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2parser.api.dto.lib.AbstractHttp2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2FrameType;

public class UnknownFrame extends AbstractHttp2Frame {

	private byte flagsByte;
	private byte frameTypeId;
	private DataWrapper framePayloadData;

	public UnknownFrame(byte flagsByte, byte frameTypeId, int streamId, DataWrapper framePayloadData) {
		this.flagsByte = flagsByte;
		this.frameTypeId = frameTypeId;
		setStreamId(streamId);
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

	public DataWrapper getFramePayloadData() {
		return framePayloadData;
	}

	@Override
	public String toString() {
		return "UnknownFrame ["
				+ super.toString()
				+ ", flagsByte=" + flagsByte + ", frameTypeId=" + frameTypeId
				+ ", framePayloadData=" + framePayloadData.getReadableSize() + "]";
	}
	
}
