package com.webpieces.http2parser.api.dto;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.impl.ByteBufferDataWrapper;

import java.nio.ByteBuffer;

public class Http2GoAway extends Http2Frame {

	public Http2FrameType getFrameType() {
		return Http2FrameType.GOAWAY;
	}

	/* flags */
	public byte getFlagsByte() {
		return 0x0;
	}
	public void setFlags(byte flags) {}

	/* payload */
	// 1 bit reserved
	private int lastStreamId; // 31bits
	private Http2ErrorCode errorCode; //32bits
	private DataWrapper debugData;

	protected DataWrapper getPayloadDataWrapper() {
		ByteBuffer prelude = ByteBuffer.allocate(8);
		prelude.putInt(lastStreamId).putInt(errorCode.getCode());

		return dataGen.chainDataWrappers(
				new ByteBufferDataWrapper(prelude),
				debugData
		);
	}


}
