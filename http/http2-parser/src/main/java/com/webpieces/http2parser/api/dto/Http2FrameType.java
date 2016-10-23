package com.webpieces.http2parser.api.dto;

import java.util.Optional;

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

    public static Optional<Http2FrameType> fromId(int id) {
        switch (id) {
            case 0x0:
                return Optional.of(DATA);
            case 0x1:
                return Optional.of(HEADERS);
            case 0x2:
                return Optional.of(PRIORITY);
            case 0x3:
                return Optional.of(RST_STREAM);
            case 0x4:
                return Optional.of(SETTINGS);
            case 0x5:
                return Optional.of(PUSH_PROMISE);
            case 0x6:
                return Optional.of(PING);
            case 0x7:
                return Optional.of(GOAWAY);
            case 0x8:
                return Optional.of(WINDOW_UPDATE);
            case 0x9:
                return Optional.of(CONTINUATION);
            default:
                return Optional.empty();
        }
    }
}
