package com.webpieces.http2parser.api.dto;

import org.webpieces.data.api.DataWrapper;

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

	public DataWrapper getPayloadDataWrapper() {
		byte[] prelude = new byte[8];
		prelude[0] = (byte) (lastStreamId >> 24);
		prelude[1] = (byte) (lastStreamId >> 16);
		prelude[2] = (byte) (lastStreamId >> 8);
		prelude[3] = (byte) lastStreamId;
		long code = errorCode.getCode();
		prelude[4] = (byte) (code >> 24);
		prelude[5] = (byte) (code >> 16);
		prelude[6] = (byte) (code >> 8);
		prelude[7] = (byte) code;

		return dataGen.chainDataWrappers(
				dataGen.wrapByteArray(prelude),
				debugData
		);
	}


}
