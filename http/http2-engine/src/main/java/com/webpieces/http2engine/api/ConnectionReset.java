package com.webpieces.http2engine.api;

import com.webpieces.http2parser.api.ConnectionException;
import com.webpieces.http2parser.api.dto.RstStreamFrame;
import com.webpieces.http2parser.api.dto.lib.Http2MsgType;

/**
 * when a connection reset occurs, we need to reset all streams on the client
 *
 */
public class ConnectionReset extends RstStreamFrame {

	private ConnectionException reason;

	public ConnectionReset(ConnectionException reason) {
		this.reason = reason;
	}

	public ConnectionException getReason() {
		return reason;
	}

	@Override
	public Http2MsgType getMessageType() {
		throw new UnsupportedOperationException("This is not a real http frame and is for notifying client");
	}

	@Override
	public boolean isEndOfStream() {
		throw new UnsupportedOperationException("This is not a real http frame and is for notifying client");
	}

	@Override
	public int getStreamId() {
		throw new UnsupportedOperationException("This is not a real http frame and is for notifying client");
	}

	@Override
	public void setStreamId(int streamId) {
		throw new UnsupportedOperationException("This is not a real http frame and is for notifying client");
	}

}
