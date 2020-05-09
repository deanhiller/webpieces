package com.webpieces.http2engine.api.error;

import com.webpieces.http2parser.api.dto.CancelReason;
import com.webpieces.http2parser.api.dto.lib.Http2MsgType;

public class ShutdownStream implements CancelReason {

	private int streamId;
	private ConnectionCancelled cause;

	public ShutdownStream(int streamId, ConnectionCancelled reset2) {
		this.streamId = streamId;
		this.cause = reset2;
	}

	@Override
	public int getStreamId() {
		return streamId;
	}

	@Override
	public Http2MsgType getMessageType() {
		return Http2MsgType.APP_CANCEL_STREAM;
	}

	public ConnectionCancelled getCause() {
		return cause;
	}

	@Override
	public boolean isEndOfStream() {
		return true;
	}

	@Override
	public void setStreamId(int streamId) {
		this.streamId = streamId;
	}

}
