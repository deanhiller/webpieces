package com.webpieces.http2parser.api.dto;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.impl.ByteBufferDataWrapper;

import java.nio.ByteBuffer;

public class Http2PushPromise extends Http2Frame {

	public Http2FrameType getFrameType() {
		return Http2FrameType.PUSH_PROMISE;
	}

	/* flags */
	private boolean endHeaders; /* 0x4 */
	private boolean padded; /* 0x8 */
	public byte getFlagsByte() {
		byte value = 0x0;
		if(endHeaders) value |= 0x4;
		if(padded) value |= 0x8;
		return value;
	}
	public void setFlags(byte flags) {
		endHeaders = (flags & 0x4) == 0x4;
		padded = (flags & 0x8) == 0x8;
	}

	/* payload */
	// reserved - 1bit
	private int promisedStreamId; //31bits
	private Http2HeaderBlock headerBlock;
	private byte[] padding;

	protected DataWrapper getPayloadDataWrapper() {
		ByteBuffer prelude = ByteBuffer.allocate(4);
		prelude.putInt(promisedStreamId);

		DataWrapper headersDW = headerBlock.getDataWrapper();
		DataWrapper finalDW = dataGen.chainDataWrappers(
				new ByteBufferDataWrapper(prelude),
				headersDW);
		if(padded)
			return finalDW;
		else
			return pad(padding, finalDW);
	}
}
