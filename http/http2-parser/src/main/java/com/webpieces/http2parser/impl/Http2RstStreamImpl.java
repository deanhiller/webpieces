package com.webpieces.http2parser.impl;

import com.webpieces.http2parser.api.Http2FrameType;
import com.webpieces.http2parser.api.Http2RstStream;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.impl.ByteBufferDataWrapper;

import java.nio.ByteBuffer;

public class Http2RstStreamImpl extends Http2FrameImpl implements Http2RstStream {
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

	public Http2ErrorCode getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(Http2ErrorCode errorCode) {
		this.errorCode = errorCode;
	}

	public DataWrapper getPayloadDataWrapper() {
		ByteBuffer payload = ByteBuffer.allocate(4);
		payload.putInt(errorCode.getCode());
		payload.flip();

		return new ByteBufferDataWrapper(payload);
	}

	public void setPayloadFromDataWrapper(DataWrapper payload) {
		ByteBuffer payloadByteBuffer = ByteBuffer.wrap(payload.createByteArray());
		errorCode = Http2ErrorCode.fromInteger(payloadByteBuffer.getInt());
	}
	
}
