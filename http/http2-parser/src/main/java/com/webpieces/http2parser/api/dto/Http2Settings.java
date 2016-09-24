package com.webpieces.http2parser.api.dto;

import org.webpieces.data.api.DataWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

        private int id;

        Parameter(int id) {
            this.id = id;
        }
        public int getId() {
            return id;
        }
	}
	// id 16bits
    // value 32bits
	private Map<Parameter, Long> settings;
    protected DataWrapper getPayloadDataWrapper() {
        ByteArrayOutputStream out = new ByteArrayOutputStream(6 * settings.size());

        for(Map.Entry<Parameter, Long> setting: settings.entrySet()) {
            byte[] ret = new byte[6];
            int id = setting.getKey().getId();
            Long value = setting.getValue();
            ret[0] = (byte) (id >> 8);
            ret[1] = (byte) id;
            ret[2] = (byte) (value >> 24);
            ret[3] = (byte) (value >> 16);
            ret[4] = (byte) (value >> 8);
            ret[5] = (byte) value.longValue();
            try {
                out.write(ret);
            } catch (IOException e) {
                // TODO: handle exception
            }
        }
        return dataGen.wrapByteArray(out.toByteArray());
    }
}
