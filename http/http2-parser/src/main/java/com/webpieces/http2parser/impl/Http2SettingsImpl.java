package com.webpieces.http2parser.impl;

import com.webpieces.http2parser.api.Http2FrameType;
import com.webpieces.http2parser.api.Http2Settings;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.impl.ByteBufferDataWrapper;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Http2SettingsImpl extends Http2FrameImpl implements Http2Settings {
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
        if (ack) value |= 0x1;
        return value;
    }

    public void setFlags(byte flags) {
        ack = (flags & 0x1) == 0x1;
    }

    /* payload */


    // id 16bits
    // value 32bits
    private Map<Http2Settings.Parameter, Integer> settings = new LinkedHashMap<>();

    public DataWrapper getPayloadDataWrapper() {
        if (ack) {
            // If ack then settings must be empty
            return dataGen.emptyWrapper();
        } else {
            ByteBuffer payload = ByteBuffer.allocate(6 * settings.size());

            for (Map.Entry<Http2Settings.Parameter, Integer> setting : settings.entrySet()) {
                short id = setting.getKey().getId();
                Integer value = setting.getValue();
                payload.putShort(id).putInt(value);
            }
            payload.flip();

            return new ByteBufferDataWrapper(payload);
        }
    }

    public void setPayloadFromDataWrapper(DataWrapper payload) {
        ByteBuffer payloadByteBuffer = ByteBuffer.wrap(payload.createByteArray());
        while (payloadByteBuffer.hasRemaining()) {
            settings.put(
                    Http2Settings.Parameter.fromId(payloadByteBuffer.getShort()),
                    payloadByteBuffer.getInt());
        }
    }

    public void setSetting(Http2Settings.Parameter param, Integer value) {
        settings.put(param, value);
    }

    public Map<Http2Settings.Parameter, Integer> getSettings() {
        if (!ack) {
            return settings;
        } else {
            return Collections.emptyMap();
        }
    }
}
