package com.webpieces.http2parser.impl;

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

    private int code;
    Http2ErrorCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
    static public Http2ErrorCode fromInteger(int code) {
        switch (code) {
            case 0x0: return NO_ERROR;
            case 0x1: return PROTOCOL_ERROR;
            case 0x2: return INTERNAL_ERROR;
            case 0x3: return FLOW_CONTROL_ERROR;
            case 0x4: return SETTINGS_TIMEOUT;
            case 0x5: return STREAM_CLOSED;
            case 0x6: return FRAME_SIZE_ERROR;
            case 0x7: return REFUSED_STREAM;
            case 0x8: return CANCEL;
            case 0x9: return COMPRESSION_ERROR;
            case 0xA: return CONNECT_ERROR;
            case 0xB: return ENHANCE_YOUR_CALM;
            case 0xC: return INADEQUATE_SECURITY;
            case 0xD: return HTTP_1_1_REQUIRED;
            default: return NO_ERROR; // TODO: throw here?
        }
    }
}
