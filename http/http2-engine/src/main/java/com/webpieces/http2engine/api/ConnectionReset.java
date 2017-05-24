package com.webpieces.http2engine.api;

import com.webpieces.http2parser.api.dto.GoAwayFrame;
import com.webpieces.http2parser.api.dto.RstStreamFrame;
import com.webpieces.http2parser.api.dto.error.ConnectionException;
import com.webpieces.http2parser.api.dto.lib.Http2MsgType;

/**
 * when a connection reset occurs, we need to reset all streams on the client
 *
 */
public class ConnectionReset extends RstStreamFrame {

	private ConnectionException cause;
	private String reason;
	private boolean farEndClosed;
	private GoAwayFrame goAwayFrame;

	public ConnectionReset(ConnectionException cause) {
		this.cause = cause;
		this.reason = cause.getMessage();
	}

	public ConnectionReset(String message, GoAwayFrame goAwayFrame, boolean farEndClosed) {
		this.reason = message;
		this.goAwayFrame = goAwayFrame;
		this.farEndClosed = farEndClosed;
	}
	
	public ConnectionException getCause() {
		return cause;
	}
	public String getReason() {
		return reason;
	}

	public boolean isFarEndClosed() {
		return farEndClosed;
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

	public GoAwayFrame getGoAwayFrame() {
		return goAwayFrame;
	}

}
