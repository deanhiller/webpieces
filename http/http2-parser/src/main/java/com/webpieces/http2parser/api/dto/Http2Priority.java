package com.webpieces.http2parser.api.dto;

import org.webpieces.data.api.DataWrapper;

public class Http2Priority extends Http2Frame {
	public Http2FrameType getFrameType() {
		return Http2FrameType.PRIORITY;
	}

	/* flags */
	public byte getFlagsByte() {
		return 0x0;
	}
	public void setFlags(byte flags) {}

	/* payload */
	private boolean streamDependencyIsExclusive; //1 bit
	private int streamDependency; //31 bits
	private short weight; //8

	protected DataWrapper getPayloadDataWrapper() {
		byte[] payload = new byte[5];
		payload[0] = (byte) (streamDependency >> 24);
		payload[1] = (byte) (streamDependency >> 16);
		payload[2] = (byte) (streamDependency >> 8);
		payload[3] = (byte) streamDependency;
		if(streamDependencyIsExclusive) payload[0] |= 0x8;
		payload[4] = (byte) weight;
		return dataGen.wrapByteArray(payload);
	}
	
}
