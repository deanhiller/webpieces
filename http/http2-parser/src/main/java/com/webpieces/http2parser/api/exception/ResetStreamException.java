package com.webpieces.http2parser.api.exception;

import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;

public class ResetStreamException extends RuntimeException {

	private Http2ErrorCode code;
	private ErrorType type;

	public ResetStreamException(String msg, Http2ErrorCode code, ErrorType type) {
		super(msg);
		this.code = code;
		this.type = type;
	}

	public Http2ErrorCode getCode() {
		return code;
	}

	public ErrorType getType() {
		return type;
	}
	
}
