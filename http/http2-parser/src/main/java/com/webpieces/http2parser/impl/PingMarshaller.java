package com.webpieces.http2parser.impl;

import java.nio.ByteBuffer;
import java.util.Optional;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.impl.ByteBufferDataWrapper;

import com.webpieces.http2parser.api.dto.PingFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;

public class PingMarshaller extends FrameMarshallerImpl {
    public PingMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen) {
        super(bufferPool, dataGen);
    }

    @Override
    public byte marshalFlags(Http2Frame frame) {
        PingFrame castFrame = (PingFrame) frame;

        byte value = 0x0;
        if (castFrame.isPingResponse()) value |= 0x1;
        return value;
    }

    @Override
    public DataWrapper marshalPayload(Http2Frame frame) {
        PingFrame castFrame = (PingFrame) frame;

        ByteBuffer payload = bufferPool.nextBuffer(8);
        payload.putLong(castFrame.getOpaqueData());
        payload.flip();

        return new ByteBufferDataWrapper(payload);
    }

    @Override
    public void unmarshalFlagsAndPayload(Http2Frame frame, byte flags, Optional<DataWrapper> maybePayload) {
        PingFrame castFrame = (PingFrame) frame;
        castFrame.setIsPingResponse((flags & 0x1) == 0x1);
        maybePayload.ifPresent(payload -> {
            ByteBuffer payloadByteBuffer = bufferPool.createWithDataWrapper(payload);

            castFrame.setOpaqueData(payloadByteBuffer.getLong());

            bufferPool.releaseBuffer(payloadByteBuffer);
        });
    }

}
