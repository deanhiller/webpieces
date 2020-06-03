package com.webpieces.http2.api.dto.error;

public class ConnectionException extends Http2Exception {

	private static final long serialVersionUID = 1L;

	public ConnectionException(CancelReasonCode reason, String logId, int streamId, String msg) {
		super(reason, logId, streamId, msg);
	}

	public ConnectionException(CancelReasonCode reason, String logId, int streamId, String msg, Throwable e) {
		super(reason, logId, streamId, msg, e);
	}

	public ConnectionException(CancelReasonCode reason, int streamId, String msg) {
		super(reason, streamId, msg);
	}
	
	@Override
	public ErrorType getErrorType() {
		return ErrorType.CONNECTION;
	}

}
