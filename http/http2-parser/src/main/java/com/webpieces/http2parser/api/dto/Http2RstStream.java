package com.webpieces.http2parser.api.dto;

import org.webpieces.data.api.DataWrapper;

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
	private long errorCode; //32 bits
	protected DataWrapper getPayloadDataWrapper() {
		byte[] payload = new byte[4];
		payload[0] = (byte) (errorCode >> 24);
		payload[1] = (byte) (errorCode >> 16);
		payload[2] = (byte) (errorCode >> 8);
		payload[3] = (byte) errorCode;

		return dataGen.wrapByteArray(payload);
	}
	
}
