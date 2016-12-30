package com.webpieces.http2parser.impl;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Optional;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import com.webpieces.http2parser.api.Http2SettingsMap;
import com.webpieces.http2parser.api.dto.AbstractHttp2Frame;
import com.webpieces.http2parser.api.dto.Http2Settings;

public class SettingsMarshaller extends FrameMarshallerImpl {

    SettingsMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen) {
        super(bufferPool, dataGen);
    }

    @Override
    public DataWrapper marshalPayload(AbstractHttp2Frame frame) {
        Http2Settings castFrame = (Http2Settings) frame;

        if (castFrame.isAck() || castFrame.getSettings().size() == 0) {
            // If ack or empty settings
            return dataGen.emptyWrapper();
        } else {
            Http2SettingsMap settings = castFrame.getSettings();
            ByteBuffer payload = bufferPool.nextBuffer(6 * settings.size());

            for (Map.Entry<Http2Settings.Parameter, Long> setting : settings.entrySet()) {
                short id = setting.getKey().getId();
                Long value = setting.getValue();
                payload.putShort(id).putInt(value.intValue());
            }
            payload.flip();

            return dataGen.wrapByteBuffer(payload);
        }
    }

    @Override
    public byte marshalFlags(AbstractHttp2Frame frame) {
        Http2Settings castFrame = (Http2Settings) frame;

        byte value = 0x0;
        if (castFrame.isAck()) value |= 0x1;
        return value;
    }

    @Override
    public void unmarshalFlagsAndPayload(AbstractHttp2Frame frame, byte flags, Optional<DataWrapper> maybePayload) {
        Http2Settings castFrame = (Http2Settings) frame;

        castFrame.setAck((flags & 0x1) == 0x1);

        maybePayload.ifPresent(payload -> {
            ByteBuffer payloadByteBuffer = bufferPool.createWithDataWrapper(payload);

            while (payloadByteBuffer.hasRemaining()) {
                castFrame.setSetting(
                        Http2Settings.Parameter.fromId(payloadByteBuffer.getShort()),
                        payloadByteBuffer.getInt() & 0xFFFFFFFFL);
            }

            bufferPool.releaseBuffer(payloadByteBuffer);
        });
    }
}
