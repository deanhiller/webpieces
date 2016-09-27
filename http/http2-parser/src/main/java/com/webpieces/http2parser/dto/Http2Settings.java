package com.webpieces.http2parser.dto;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.impl.ByteBufferDataWrapper;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

class Http2Settings extends Http2Frame {
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
        static public Parameter fromId(short id) {
            switch(id) {
                case 0x1: return SETTINGS_HEADER_TABLE_SIZE;
                case 0x2: return SETTINGS_ENABLE_PUSH;
                case 0x3: return SETTINGS_MAX_CONCURRENT_STREAMS;
                case 0x4: return SETTINGS_INITIAL_WINDOW_SIZE;
                case 0x5: return SETTINGS_MAX_FRAME_SIZE;
                case 0x6: return SETTINGS_MAX_HEADER_LIST_SIZE;
                default: return SETTINGS_HEADER_TABLE_SIZE; // TODO: throw here
            }
        }
	}

	// id 16bits
    // value 32bits
	private Map<Parameter, Integer> settings;
    protected DataWrapper getPayloadDataWrapper() {
        ByteBuffer payload = ByteBuffer.allocate(6 * settings.size());

        for(Map.Entry<Parameter, Integer> setting: settings.entrySet()) {
            short id = setting.getKey().getId();
            Integer value = setting.getValue();
            payload.putShort(id).putInt(value);
        }
        payload.flip();

        return new ByteBufferDataWrapper(payload);
    }

    protected void setFromPayload(DataWrapper payload) {
        ByteBuffer payloadByteBuffer = ByteBuffer.wrap(payload.createByteArray());
        while(payloadByteBuffer.hasRemaining()) {
            settings.put(
                    Parameter.fromId(payloadByteBuffer.getShort()),
                    payloadByteBuffer.getInt());
        }
    }

    public Http2Settings() {
        settings = new HashMap<>();
    }
}
