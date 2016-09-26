package com.webpieces.http2parser.api.dto;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.impl.ByteBufferDataWrapper;

import java.nio.ByteBuffer;

public class Http2Ping extends Http2Frame {
	public Http2FrameType getFrameType() {
		return Http2FrameType.PING;
	}

	/* flags */
	private boolean isPingResponse; /* 0x1 */
	public byte getFlagsByte() {
		byte value = 0x0;
		if(isPingResponse) value |= 0x1;
		return value;
	}
	public void setFlags(byte flags) {
		isPingResponse = (flags & 0x1) == 0x1;
	}

	/* payload */
	private long opaqueData;
	protected DataWrapper getPayloadDataWrapper() {
		ByteBuffer ret = ByteBuffer.allocate(8);
		ret.putLong(opaqueData);
		return new ByteBufferDataWrapper(ret);
	}

	protected void setPayload(DataWrapper payload) {
		ByteBuffer payloadByteBuffer = ByteBuffer.wrap(payload.createByteArray());
		opaqueData = payloadByteBuffer.getLong();
	}
}
