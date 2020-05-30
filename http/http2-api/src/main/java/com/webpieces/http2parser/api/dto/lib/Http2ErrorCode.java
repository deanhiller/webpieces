package com.webpieces.http2parser.api.dto.lib;

import java.util.HashMap;
import java.util.Map;

public enum Http2ErrorCode {
    NO_ERROR(0x0),
    PROTOCOL_ERROR(0x1),
    INTERNAL_ERROR(0x2),
    FLOW_CONTROL_ERROR(0x3),
    SETTINGS_TIMEOUT(0x4),
    STREAM_CLOSED(0x5),
    FRAME_SIZE_ERROR(0x6),
    REFUSED_STREAM(0x7),
    CANCEL(0x8),
    COMPRESSION_ERROR(0x9),
    CONNECT_ERROR(0xA),
    ENHANCE_YOUR_CALM(0xB),
    INADEQUATE_SECURITY(0xC),
    HTTP_1_1_REQUIRED(0xD);

	private final static Map<Long, Http2ErrorCode> codeToError = new HashMap<>(); 
    private long code;

    static { 
    	for(Http2ErrorCode c : Http2ErrorCode.values()) {
    		codeToError.put(c.getCode(), c);
    	}
    }
    
    Http2ErrorCode(long code) {
        this.code = code;
    }

    public long getCode() {
        return code;
    }

    public static Http2ErrorCode translate(long code) {
    	Http2ErrorCode http2ErrorCode = codeToError.get(code);
    	if(http2ErrorCode == null)
    		throw new IllegalArgumentException("code="+code+" is not known");
    	return http2ErrorCode;
    }
}
