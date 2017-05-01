package com.webpieces.http2parser.api;

public class StreamException extends Http2Exception {

	private static final long serialVersionUID = 1L;

	public StreamException(ParseFailReason reason, int streamId, String msg) {
		super(reason, streamId, msg);
	}

	public StreamException(ParseFailReason reason, int streamId, String msg, Throwable e) {
		super(reason, streamId, msg, e);
	}

	@Override
	public ErrorType getErrorType() {
		return ErrorType.STREAM;
	}

}
