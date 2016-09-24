package com.webpieces.http2parser.api.dto;

import org.webpieces.data.api.DataWrapper;

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
	public DataWrapper getPayloadDataWrapper() {
		byte[] ret = new byte[8];
		ret[0] = (byte) (opaqueData >> 56);
		ret[1] = (byte) (opaqueData >> 48);
		ret[2] = (byte) (opaqueData >> 40);
		ret[3] = (byte) (opaqueData >> 32);
		ret[4] = (byte) (opaqueData >> 24);
		ret[5] = (byte) (opaqueData >> 16);
		ret[6] = (byte) (opaqueData >> 8);
		ret[7] = (byte) opaqueData;
		return dataGen.wrapByteArray(ret);
	}
}
