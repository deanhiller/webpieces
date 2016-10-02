package com.webpieces.http2parser.impl;

import com.webpieces.http2parser.api.dto.Http2Frame;
import com.webpieces.http2parser.api.dto.Http2Settings;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Optional;

public class SettingsMarshaller extends FrameMarshallerImpl {

    SettingsMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen) {
        super(bufferPool, dataGen);
    }

    public DataWrapper marshalPayload(Http2Frame frame) {
        Http2Settings castFrame = (Http2Settings) frame;

        if (castFrame.isAck() || castFrame.getSettings().size() == 0) {
            // If ack or empty settings
            return dataGen.emptyWrapper();
        } else {
            Map<Http2Settings.Parameter, Integer> settings = castFrame.getSettings();
            ByteBuffer payload = bufferPool.nextBuffer(6 * settings.size());

            for (Map.Entry<Http2Settings.Parameter, Integer> setting : settings.entrySet()) {
                short id = setting.getKey().getId();
                Integer value = setting.getValue();
                payload.putShort(id).putInt(value);
            }
            payload.flip();

            return dataGen.wrapByteBuffer(payload);
        }
    }

    public byte marshalFlags(Http2Frame frame) {
        Http2Settings castFrame = (Http2Settings) frame;

        byte value = 0x0;
        if (castFrame.isAck()) value |= 0x1;
        return value;
    }

    public void unmarshalFlagsAndPayload(Http2Frame frame, byte flags, Optional<DataWrapper> maybePayload) {
        Http2Settings castFrame = (Http2Settings) frame;

        castFrame.setAck((flags & 0x1) == 0x1);

        maybePayload.ifPresent(payload -> {
            ByteBuffer payloadByteBuffer = bufferPool.createWithDataWrapper(payload);

            while (payloadByteBuffer.hasRemaining()) {
                castFrame.setSetting(
                        Http2Settings.Parameter.fromId(payloadByteBuffer.getShort()),
                        payloadByteBuffer.getInt());
            }

            bufferPool.releaseBuffer(payloadByteBuffer);
        });
    }
}
