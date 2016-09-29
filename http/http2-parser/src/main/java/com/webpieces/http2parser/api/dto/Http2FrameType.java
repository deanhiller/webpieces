package com.webpieces.http2parser.api.dto;

public enum Http2FrameType {
    DATA(0x0),
    HEADERS(0x1),
    PRIORITY(0x2),
    RST_STREAM(0x3),
    SETTINGS(0x4),
    PUSH_PROMISE(0x5),
    PING(0x6),
    GOAWAY(0x7),
    WINDOW_UPDATE(0x8),
    CONTINUATION(0x9);

    private final int id;

    Http2FrameType(int id) {
        this.id = id;
    }

    public byte getId() {
        return (byte) id;
    }

    public static Http2FrameType fromId(int id) {
        switch (id) {
            case 0x0:
                return DATA;
            case 0x1:
                return HEADERS;
            case 0x2:
                return PRIORITY;
            case 0x3:
                return RST_STREAM;
            case 0x4:
                return SETTINGS;
            case 0x5:
                return PUSH_PROMISE;
            case 0x6:
                return PING;
            case 0x7:
                return GOAWAY;
            case 0x8:
                return WINDOW_UPDATE;
            case 0x9:
                return CONTINUATION;
            default:
                return DATA; // TODO: throw here
        }
    }
}
