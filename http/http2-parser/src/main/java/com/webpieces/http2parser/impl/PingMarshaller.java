package com.webpieces.http2parser.impl;

import com.webpieces.http2parser.api.dto.Http2Frame;
import com.webpieces.http2parser.api.dto.Http2Ping;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.impl.ByteBufferDataWrapper;

import java.nio.ByteBuffer;

public class PingMarshaller extends FrameMarshallerImpl {
    PingMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen) {
        super(bufferPool, dataGen);
    }

    public byte getFlagsByte(Http2Frame frame) {
        Http2Ping castFrame = (Http2Ping) frame;

        byte value = 0x0;
        if (castFrame.isPingResponse()) value |= 0x1;
        return value;
    }
    public DataWrapper getPayloadDataWrapper(Http2Frame frame) {
        Http2Ping castFrame = (Http2Ping) frame;

        ByteBuffer payload = bufferPool.nextBuffer(8);
        payload.putLong(castFrame.getOpaqueData());
        payload.flip();

        return new ByteBufferDataWrapper(payload);
    }
}
