package com.webpieces.http2.api.dto.lowlevel;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2.api.dto.lowlevel.lib.AbstractHttp2Frame;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2FrameType;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2MsgType;
import com.webpieces.http2.api.dto.lowlevel.lib.StreamMsg;

public class UnknownFrame extends AbstractHttp2Frame implements StreamMsg {

	private byte flagsByte;
	private byte frameTypeId;
	private DataWrapper framePayloadData;

	public UnknownFrame(byte flagsByte, byte frameTypeId, int streamId, DataWrapper framePayloadData) {
		this.flagsByte = flagsByte;
		this.frameTypeId = frameTypeId;
		setStreamId(streamId);
		this.framePayloadData = framePayloadData;
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
	public boolean isEndOfStream() {
		return false;
	}
	
	@Override
	public Http2FrameType getFrameType() {
		throw new UnsupportedOperationException("not supported yet");
	}
	@Override
	public Http2MsgType getMessageType() {
		return Http2MsgType.UNKNOWN;
	}
	
	@Override
	public String toString() {
		return "UnknownFrame ["
				+ super.toString()
				+ ", flagsByte=" + flagsByte + ", frameTypeId=" + frameTypeId
				+ ", framePayloadData=" + framePayloadData.getReadableSize() + "]";
	}
	
}
