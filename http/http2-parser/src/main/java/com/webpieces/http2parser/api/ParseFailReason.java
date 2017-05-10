package com.webpieces.http2parser.api;

import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;

public enum ParseFailReason {
	HEADERS_MIXED_WITH_FRAMES(Http2ErrorCode.PROTOCOL_ERROR),
	HEADER_DECODE(Http2ErrorCode.COMPRESSION_ERROR),
	HEADER_NOT_LOWER_CASE(Http2ErrorCode.PROTOCOL_ERROR),
	INVALID_STREAM_ID(Http2ErrorCode.PROTOCOL_ERROR),
	BAD_STREAM_DEPENDENCY(Http2ErrorCode.PROTOCOL_ERROR),
	NOT_ENOUGH_PAD_DATA(Http2ErrorCode.PROTOCOL_ERROR),
	
	EXCEEDED_MAX_FRAME_SIZE(Http2ErrorCode.FRAME_SIZE_ERROR),
	FRAME_SIZE_INCORRECT(Http2ErrorCode.FRAME_SIZE_ERROR),
	
	INVALID_SETTING(Http2ErrorCode.PROTOCOL_ERROR),
	
	WINDOW_SIZE_INVALID(Http2ErrorCode.PROTOCOL_ERROR),
	SETTINGS_WINDOW_SIZE_INVALID(Http2ErrorCode.FLOW_CONTROL_ERROR),

	FAR_END_CLOSED_SOCKET(Http2ErrorCode.NO_ERROR),
	FLOW_CONTROL_ERROR(Http2ErrorCode.FLOW_CONTROL_ERROR),
	BAD_FRAME_RECEIVED_FOR_THIS_STATE(Http2ErrorCode.PROTOCOL_ERROR), 
	BUG(Http2ErrorCode.INTERNAL_ERROR),
	CLOSED_STREAM(Http2ErrorCode.STREAM_CLOSED),
	;

	private Http2ErrorCode errorCode;

	private ParseFailReason(Http2ErrorCode errorCode) {
		this.errorCode = errorCode;
	}
	
	public Http2ErrorCode getErrorCode() {
		return errorCode;
	}

}
