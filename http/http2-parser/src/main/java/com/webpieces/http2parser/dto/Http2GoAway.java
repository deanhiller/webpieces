package com.webpieces.http2parser.dto;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.impl.ByteBufferDataWrapper;

import java.nio.ByteBuffer;
import java.util.List;

class Http2GoAway extends Http2Frame {

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

	private void setLastStreamId(int lastStreamId) {
		this.lastStreamId = lastStreamId & 0x7FFFFFFF; // clear the MSB for reserved
	}

	private void setErrorCode(Http2ErrorCode errorCode) {
		this.errorCode = errorCode;
	}

	protected DataWrapper getPayloadDataWrapper() {
		ByteBuffer prelude = ByteBuffer.allocate(8);
		prelude.putInt(lastStreamId).putInt(errorCode.getCode());
		prelude.flip();

		return dataGen.chainDataWrappers(
				new ByteBufferDataWrapper(prelude),
				debugData
		);
	}

	protected void setFromPayload(DataWrapper payload) {
		List<? extends DataWrapper> split = dataGen.split(payload, 8);
		ByteBuffer preludeBytes = ByteBuffer.wrap(split.get(0).createByteArray());
		setLastStreamId(preludeBytes.getInt());
		setErrorCode(Http2ErrorCode.fromInteger(preludeBytes.getInt()));

		debugData = split.get(1);
	}


}
