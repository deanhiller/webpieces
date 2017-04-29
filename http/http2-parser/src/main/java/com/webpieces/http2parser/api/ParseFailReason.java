package com.webpieces.http2parser.api;

import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;

public enum ParseFailReason {
	HEADERS_MIXED_WITH_FRAMES(Http2ErrorCode.PROTOCOL_ERROR, true),
	HEADER_DECODE(Http2ErrorCode.COMPRESSION_ERROR, false),
	HEADER_NOT_LOWER_CASE(Http2ErrorCode.PROTOCOL_ERROR, false),
	INVALID_STREAM_ID(Http2ErrorCode.PROTOCOL_ERROR, true),
	BAD_STREAM_DEPENDENCY(Http2ErrorCode.PROTOCOL_ERROR, true),
	NOT_ENOUGH_PAD_DATA(Http2ErrorCode.PROTOCOL_ERROR, true),
	FRAME_SIZE_INCORRECT_CONNECTION(Http2ErrorCode.FRAME_SIZE_ERROR, true),
	FRAME_SIZE_INCORRECT_STREAM(Http2ErrorCode.FRAME_SIZE_ERROR, false),
	INVALID_SETTING(Http2ErrorCode.PROTOCOL_ERROR, true),
	WINDOW_SIZE_INVALID(Http2ErrorCode.PROTOCOL_ERROR, true),
	WINDOW_SIZE_INVALID2(Http2ErrorCode.FLOW_CONTROL_ERROR, true),

	FAR_END_CLOSED_SOCKET(Http2ErrorCode.NO_ERROR, true),
	FLOW_CONTROL_ERROR_CONNECTION(Http2ErrorCode.FLOW_CONTROL_ERROR, true), 
	FLOW_CONTROL_ERROR_STREAM(Http2ErrorCode.FLOW_CONTROL_ERROR, false), 

	;

	private Http2ErrorCode errorCode;
	private ErrorType errorType;

	private ParseFailReason(Http2ErrorCode errorCode, boolean isConnectionLevelError) {
		this.errorCode = errorCode;
		if(isConnectionLevelError)
			errorType = ErrorType.CONNECTION;
		else
			errorType = ErrorType.STREAM;
	}
	
	public Http2ErrorCode getErrorCode() {
		return errorCode;
	}

	public ErrorType getErrorType() {
		return errorType;
	}

}
