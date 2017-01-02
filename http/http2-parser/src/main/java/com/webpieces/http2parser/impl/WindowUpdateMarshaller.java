package com.webpieces.http2parser.impl;

import java.nio.ByteBuffer;
import java.util.Optional;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import com.webpieces.http2parser.api.dto.WindowUpdateFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;

public class WindowUpdateMarshaller extends FrameMarshallerImpl {

    WindowUpdateMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen) {
        super(bufferPool, dataGen);
    }

    @Override
    public byte marshalFlags(Http2Frame frame) {
        return 0x0;
    }

    @Override
    public DataWrapper marshalPayload(Http2Frame frame) {
        WindowUpdateFrame castFrame = (WindowUpdateFrame) frame;

        ByteBuffer payload = bufferPool.nextBuffer(4).putInt(castFrame.getWindowSizeIncrement());
        payload.flip();

        return dataGen.wrapByteBuffer(payload);
    }

    @Override
    public void unmarshalFlagsAndPayload(Http2Frame frame, byte flags, Optional<DataWrapper> maybePayload) {
        WindowUpdateFrame castFrame = (WindowUpdateFrame) frame;
        maybePayload.ifPresent(payload -> {
            ByteBuffer payloadByteBuffer = bufferPool.createWithDataWrapper(payload);

            castFrame.setWindowSizeIncrement(payloadByteBuffer.getInt());
            bufferPool.releaseBuffer(payloadByteBuffer);

        });
    }
}
