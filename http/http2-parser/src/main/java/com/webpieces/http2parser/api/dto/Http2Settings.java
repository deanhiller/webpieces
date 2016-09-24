package com.webpieces.http2parser.api.dto;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.impl.ByteBufferDataWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

public class Http2Settings extends Http2Frame {
	public Http2FrameType getFrameType() {
        return Http2FrameType.SETTINGS;
    }

	/* flags */
	private boolean ack; /* 0x1 */
	public byte getFlagsByte() {
        byte value = 0x0;
        if(ack) value |= 0x1;
        return value;
    }
    public void setFlags(byte flags) {
        ack = (flags & 0x1) == 0x1;
    }

    /* payload */
    private enum Parameter {
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
	}
	// id 16bits
    // value 32bits
	private Map<Parameter, Long> settings;
    protected DataWrapper getPayloadDataWrapper() {
        ByteBuffer ret = ByteBuffer.allocate(6 * settings.size());

        for(Map.Entry<Parameter, Long> setting: settings.entrySet()) {
            short id = setting.getKey().getId();
            Long value = setting.getValue();
            ret.putShort(id).putInt(value.intValue());
        }
        return new ByteBufferDataWrapper(ret);
    }
}
