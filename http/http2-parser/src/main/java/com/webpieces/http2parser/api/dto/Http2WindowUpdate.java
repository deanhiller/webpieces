package com.webpieces.http2parser.api.dto;

import org.webpieces.data.api.DataWrapper;

public class Http2WindowUpdate extends Http2Frame {
	public Http2FrameType getFrameType() {
		return Http2FrameType.WINDOW_UPDATE;
	}
	/* flags */
	public byte getFlagsByte() {
		return 0x0;
	}
	public void setFlags(byte flags) {}

	/* payload */
	//1bit reserved
	private int windowSizeIncrement; //31 bits
	protected DataWrapper getPayloadDataWrapper() {
		byte[] ret = new byte[4];
		ret[0] = (byte) (windowSizeIncrement >> 24);
		ret[1] = (byte) (windowSizeIncrement >> 16);
		ret[2] = (byte) (windowSizeIncrement >> 8);
		ret[3] = (byte) windowSizeIncrement;
		return dataGen.wrapByteArray(ret);
	}
	
}
