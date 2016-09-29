package com.webpieces.http2parser.impl;

import com.webpieces.http2parser.api.FrameMarshaller;
import com.webpieces.http2parser.api.dto.Http2Frame;
import com.webpieces.http2parser.api.dto.Http2Priority;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import java.nio.ByteBuffer;

public class PriorityMarshaller implements FrameMarshaller {
    private BufferPool bufferPool;
    private DataWrapperGenerator dataGen;

    PriorityMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen) {
        this.bufferPool = bufferPool;
        this.dataGen = dataGen;
    }

    public byte getFlagsByte(Http2Frame frame) {
        return 0x0;
    }

    public DataWrapper getPayloadDataWrapper(Http2Frame frame) {
        Http2Priority castFrame = (Http2Priority) frame;

        ByteBuffer payload = bufferPool.nextBuffer(5);
        payload.putInt(castFrame.getStreamDependency());
        if (castFrame.isStreamDependencyIsExclusive()) payload.put(0, (byte) (payload.get(0) | 0x80));
        payload.put(castFrame.getWeight());
        payload.flip();

        return dataGen.wrapByteBuffer(payload);
    }

}
