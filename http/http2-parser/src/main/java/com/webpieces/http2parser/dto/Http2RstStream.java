package com.webpieces.http2parser.dto;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.impl.ByteBufferDataWrapper;

import java.nio.ByteBuffer;

class Http2RstStream extends Http2Frame {
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
		payload.flip();

		return new ByteBufferDataWrapper(payload);
	}

	protected void setFromPayload(DataWrapper payload) {
		ByteBuffer payloadByteBuffer = ByteBuffer.wrap(payload.createByteArray());
		errorCode = Http2ErrorCode.fromInteger(payloadByteBuffer.getInt());
	}
	
}
