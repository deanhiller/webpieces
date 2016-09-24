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
}
