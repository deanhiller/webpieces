package com.webpieces.http2parser.api;

import java.util.Map;

public interface Http2Settings extends Http2Frame {
    enum Parameter {
        SETTINGS_HEADER_TABLE_SIZE(0x1),
        SETTINGS_ENABLE_PUSH(0x2),
        SETTINGS_MAX_CONCURRENT_STREAMS(0x3),
        SETTINGS_INITIAL_WINDOW_SIZE(0x4),
        SETTINGS_MAX_FRAME_SIZE(0x5),
        SETTINGS_MAX_HEADER_LIST_SIZE(0x6);

        private short id;

        Parameter(int id) {
            this.id = (short) id;
        }

        public short getId() {
            return id;
        }

        public static Parameter fromId(short id) {
            switch (id) {
                case 0x1:
                    return SETTINGS_HEADER_TABLE_SIZE;
                case 0x2:
                    return SETTINGS_ENABLE_PUSH;
                case 0x3:
                    return SETTINGS_MAX_CONCURRENT_STREAMS;
                case 0x4:
                    return SETTINGS_INITIAL_WINDOW_SIZE;
                case 0x5:
                    return SETTINGS_MAX_FRAME_SIZE;
                case 0x6:
                    return SETTINGS_MAX_HEADER_LIST_SIZE;
                default:
                    return SETTINGS_HEADER_TABLE_SIZE; // TODO: throw here
            }
        }
    }

    /* flags */
    boolean isAck();

    void setAck();

    /* payload */
    void setSetting(Http2Settings.Parameter param, Integer value);

    Map<Parameter, Integer> getSettings();
}
