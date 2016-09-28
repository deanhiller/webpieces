package com.webpieces.http2parser.impl;

import com.webpieces.http2parser.api.Http2FrameType;
import com.webpieces.http2parser.api.Http2GoAway;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.impl.ByteBufferDataWrapper;

import java.nio.ByteBuffer;
import java.util.List;

public class Http2GoAwayImpl extends Http2FrameImpl implements Http2GoAway {

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

	public void setLastStreamId(int lastStreamId) {
		this.lastStreamId = lastStreamId & 0x7FFFFFFF; // clear the MSB for reserved
	}

	public void setErrorCode(Http2ErrorCode errorCode) {
		this.errorCode = errorCode;
	}

	public int getLastStreamId() {
		return lastStreamId;
	}

	public Http2ErrorCode getErrorCode() {
		return errorCode;
	}

	public DataWrapper getDebugData() {
		return debugData;
	}

	public void setDebugData(DataWrapper debugData) {
		this.debugData = debugData;
	}

	public DataWrapper getPayloadDataWrapper() {
		ByteBuffer prelude = ByteBuffer.allocate(8);
		prelude.putInt(lastStreamId).putInt(errorCode.getCode());
		prelude.flip();

		return dataGen.chainDataWrappers(
				new ByteBufferDataWrapper(prelude),
				debugData
		);
	}

	public void setPayloadFromDataWrapper(DataWrapper payload) {
		List<? extends DataWrapper> split = dataGen.split(payload, 8);
		ByteBuffer preludeBytes = ByteBuffer.wrap(split.get(0).createByteArray());
		setLastStreamId(preludeBytes.getInt());
		setErrorCode(Http2ErrorCode.fromInteger(preludeBytes.getInt()));

		debugData = split.get(1);
	}


}
