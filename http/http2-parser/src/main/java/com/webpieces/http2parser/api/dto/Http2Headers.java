package com.webpieces.http2parser.api.dto;

import org.webpieces.data.api.DataWrapper;

public class Http2Headers extends Http2Frame {
	public Http2FrameType getFrameType() {
		return Http2FrameType.HEADERS;
	}

	/* flags */
	private boolean endStream; /* 0x1 */
	private boolean endHeaders; /* 0x4 */
	private boolean padded; /* 0x8 */
	private boolean priority; /* 0x20 */
	public byte getFlagsByte() {
		byte value = 0x0;
		if(endStream) value |= 0x1;
		if(endHeaders) value |= 0x4;
		if(padded) value |= 0x8;
		if(priority) value |= 0x20;
		return value;
	}
	public void setFlags(byte flags) {
		endStream = (flags & 0x1) == 0x1;
		endHeaders = (flags & 0x4) == 0x4;
		padded = (flags & 0x8) == 0x8;
		priority = (flags & 0x20) == 0x20;
	}

	/* payload */
	private boolean streamDependencyIsExclusive; //1 bit
	private int streamDependency; //31 bits
	private short weight; //8 bits
	private Http2HeaderBlock headerBlock;
	private byte[] padding;

	protected DataWrapper getPayloadDataWrapper() {
		byte[] prelude = new byte[5];
		prelude[0] = (byte) (streamDependency >> 24);
		prelude[1] = (byte) (streamDependency >> 16);
		prelude[2] = (byte) (streamDependency >> 8);
		prelude[3] = (byte) streamDependency;

		if(streamDependencyIsExclusive) prelude[0] |= 0x8;
		prelude[4] = (byte) weight;

		DataWrapper unpadded = dataGen.chainDataWrappers(
				dataGen.wrapByteArray(prelude),
				headerBlock.getDataWrapper());
		if(!padded) {
			return unpadded;
		}
		else {
			return pad(padding, unpadded);
		}
	}
}
