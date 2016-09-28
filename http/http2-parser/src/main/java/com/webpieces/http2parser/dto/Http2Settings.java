package com.webpieces.http2parser.dto;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.impl.ByteBufferDataWrapper;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class Http2Settings extends Http2Frame {
	public Http2FrameType getFrameType() {
        return Http2FrameType.SETTINGS;
    }

	/* flags */
	private boolean ack = false; /* 0x1 */

    public boolean isAck() {
        return ack;
    }

    public void setAck() {
        this.ack = true;

        // If ack, the settings must be empty
        this.settings.clear();
    }

    public byte getFlagsByte() {
        byte value = 0x0;
        if(ack) value |= 0x1;
        return value;
    }

    public void setFlags(byte flags) {
        ack = (flags & 0x1) == 0x1;
    }

    /* payload */
    public enum Parameter {
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

        short getId() {
            return id;
        }
        static Parameter fromId(short id) {
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
	private Map<Parameter, Integer> settings = new HashMap<>();
    protected DataWrapper getPayloadDataWrapper() {
        if(ack) {
            // If ack then settings must be empty
            return dataGen.emptyWrapper();
        } else {
            ByteBuffer payload = ByteBuffer.allocate(6 * settings.size());

            for (Map.Entry<Parameter, Integer> setting : settings.entrySet()) {
                short id = setting.getKey().getId();
                Integer value = setting.getValue();
                payload.putShort(id).putInt(value);
            }
            payload.flip();

            return new ByteBufferDataWrapper(payload);
        }
    }

    protected void setPayloadFromDataWrapper(DataWrapper payload) {
        ByteBuffer payloadByteBuffer = ByteBuffer.wrap(payload.createByteArray());
        while(payloadByteBuffer.hasRemaining()) {
            settings.put(
                    Parameter.fromId(payloadByteBuffer.getShort()),
                    payloadByteBuffer.getInt());
        }
    }

    public void setSetting(Parameter param, Integer value) {
        settings.put(param, value);
    }

    public Map<Parameter, Integer> getSettings() {
        if(!ack) {
            return settings;
        } else
        {
            return Collections.emptyMap();
        }
    }
}
