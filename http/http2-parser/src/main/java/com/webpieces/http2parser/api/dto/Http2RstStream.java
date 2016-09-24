package com.webpieces.http2parser.api.dto;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.impl.ByteBufferDataWrapper;

import java.nio.ByteBuffer;

public class Http2RstStream extends Http2Frame {
	public Http2FrameType getFrameType() {
		return Http2FrameType.RST_STREAM;
	}
	/* flags */
	public byte getFlagsByte() {
		return 0x0;
	}
	public void setFlags(byte flags) {}

	/* payload */
	private Http2ErrorCode errorCode; //32 bits
	protected DataWrapper getPayloadDataWrapper() {
		ByteBuffer payload = ByteBuffer.allocate(4);
		payload.putInt(errorCode.getCode());

		return new ByteBufferDataWrapper(payload);
	}
	
}
