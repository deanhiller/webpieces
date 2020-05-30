package com.webpieces.http2parser.api.dto.error;

public class StreamException extends Http2Exception {

	private static final long serialVersionUID = 1L;

	public StreamException(CancelReasonCode reason, String logId, int streamId, String msg) {
		super(reason, logId, streamId, msg);
	}

	public StreamException(CancelReasonCode reason, String logId, int streamId, String msg, Throwable e) {
		super(reason, logId, streamId, msg, e);
	}

	public StreamException(CancelReasonCode reason, int streamId, String msg) {
		super(reason, streamId, msg);
	}
	
	@Override
	public ErrorType getErrorType() {
		return ErrorType.STREAM;
	}

}
