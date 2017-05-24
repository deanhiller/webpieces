package com.webpieces.http2parser.api.dto.error;

public class ConnectionException extends Http2Exception {

	private static final long serialVersionUID = 1L;

	public ConnectionException(ParseFailReason reason, int streamId, String msg) {
		super(reason, streamId, msg);
	}

	public ConnectionException(ParseFailReason reason, int streamId, String msg, Throwable e) {
		super(reason, streamId, msg, e);
	}

	@Override
	public ErrorType getErrorType() {
		return ErrorType.CONNECTION;
	}

}
