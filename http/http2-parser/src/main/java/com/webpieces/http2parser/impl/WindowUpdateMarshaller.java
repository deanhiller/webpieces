package com.webpieces.http2parser.impl;

import java.nio.ByteBuffer;
import java.util.Optional;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import com.webpieces.http2parser.api.dto.AbstractHttp2Frame;
import com.webpieces.http2parser.api.dto.Http2WindowUpdate;

public class WindowUpdateMarshaller extends FrameMarshallerImpl {

    WindowUpdateMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen) {
        super(bufferPool, dataGen);
    }

    @Override
    public byte marshalFlags(AbstractHttp2Frame frame) {
        return 0x0;
    }

    @Override
    public DataWrapper marshalPayload(AbstractHttp2Frame frame) {
        Http2WindowUpdate castFrame = (Http2WindowUpdate) frame;

        ByteBuffer payload = bufferPool.nextBuffer(4).putInt(castFrame.getWindowSizeIncrement());
        payload.flip();

        return dataGen.wrapByteBuffer(payload);
    }

    @Override
    public void unmarshalFlagsAndPayload(AbstractHttp2Frame frame, byte flags, Optional<DataWrapper> maybePayload) {
        Http2WindowUpdate castFrame = (Http2WindowUpdate) frame;
        maybePayload.ifPresent(payload -> {
            ByteBuffer payloadByteBuffer = bufferPool.createWithDataWrapper(payload);

            castFrame.setWindowSizeIncrement(payloadByteBuffer.getInt());
            bufferPool.releaseBuffer(payloadByteBuffer);

        });
    }
}
