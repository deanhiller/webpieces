package com.webpieces.http2parser.impl;

import com.webpieces.http2parser.api.Http2FrameType;
import com.webpieces.http2parser.api.Http2Ping;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.impl.ByteBufferDataWrapper;

import java.nio.ByteBuffer;

public class Http2PingImpl extends Http2FrameImpl implements Http2Ping {
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

	public boolean isPingResponse() {
		return isPingResponse;
	}

	public void setPingResponse(boolean pingResponse) {
		isPingResponse = pingResponse;
	}

	/* payload */
	private long opaqueData;

	public long getOpaqueData() {
		return opaqueData;
	}

	public void setOpaqueData(long opaqueData) {
		this.opaqueData = opaqueData;
	}

	public DataWrapper getPayloadDataWrapper() {
		ByteBuffer payload = ByteBuffer.allocate(8);
		payload.putLong(opaqueData);
		payload.flip();

		return new ByteBufferDataWrapper(payload);
	}

	public void setPayloadFromDataWrapper(DataWrapper payload) {
		ByteBuffer payloadByteBuffer = ByteBuffer.wrap(payload.createByteArray());
		opaqueData = payloadByteBuffer.getLong();
	}
}
