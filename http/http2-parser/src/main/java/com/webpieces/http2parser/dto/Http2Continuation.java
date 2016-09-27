package com.webpieces.http2parser.dto;

import org.webpieces.data.api.DataWrapper;

public class Http2Continuation extends Http2Frame {

	public Http2FrameType getFrameType() {
		return Http2FrameType.CONTINUATION;
	}

	/* Flags */
	private boolean endHeaders; /* 0x4 */
	public byte getFlagsByte() {
		byte value = 0x0;
		if(endHeaders) value |= 0x4;
		return value;
	}
	public void setFlags(byte flags) {
		endHeaders = (flags & 0x4) == 0x4;
	}

	/* payload */
	private Http2HeaderBlock headerBlock;
	protected DataWrapper getPayloadDataWrapper() {
		return headerBlock.getDataWrapper();
	}

	protected void setFromPayload(DataWrapper payload)
	{
		headerBlock = new Http2HeaderBlock(payload);
	}

}
