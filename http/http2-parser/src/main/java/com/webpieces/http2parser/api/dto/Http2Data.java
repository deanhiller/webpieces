package com.webpieces.http2parser.api.dto;

import org.webpieces.data.api.DataWrapper;

import javax.xml.crypto.Data;
import java.io.ByteArrayOutputStream;

public class Http2Data extends Http2Frame {
	public Http2FrameType getFrameType() {
		return Http2FrameType.DATA;
	}

	/* flags */
	private boolean endStream; /* 0x1 */
	private boolean padded;    /* 0x8 */
	protected byte getFlagsByte() {
		byte value = (byte) 0x0;
		if(endStream) value |= 0x1;
		if(padded) value |= 0x8;
		return value;
	}
	public void setFlags(byte flags) {
		endStream = (flags & 0x1) == 0x1;
		padded = (flags & 0x8) == 0x8;
	}


	/* payload */
	private DataWrapper data;
	private byte[] padding;

	protected DataWrapper getPayloadDataWrapper() {
		if(!padded) {
			return data;
		} else {
			return pad(padding, data);
		}
	}
}
