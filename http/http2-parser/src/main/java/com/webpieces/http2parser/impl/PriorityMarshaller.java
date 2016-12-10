package com.webpieces.http2parser.impl;

import java.nio.ByteBuffer;
import java.util.Optional;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import com.webpieces.http2parser.api.FrameMarshaller;
import com.webpieces.http2parser.api.ParseException;
import com.webpieces.http2parser.api.dto.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.Http2Frame;
import com.webpieces.http2parser.api.dto.Http2Priority;

public class PriorityMarshaller implements FrameMarshaller {
    private BufferPool bufferPool;
    private DataWrapperGenerator dataGen;

    PriorityMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen) {
        this.bufferPool = bufferPool;
        this.dataGen = dataGen;
    }

    @Override
    public byte marshalFlags(Http2Frame frame) {
        return 0x0;
    }

    @Override
    public DataWrapper marshalPayload(Http2Frame frame) {
        Http2Priority castFrame = (Http2Priority) frame;

        ByteBuffer payload = bufferPool.nextBuffer(5);
        payload.putInt(castFrame.getStreamDependency());
        if (castFrame.isStreamDependencyIsExclusive()) payload.put(0, (byte) (payload.get(0) | 0x80));
        payload.put((byte) (castFrame.getWeight() & 0xFF));
        payload.flip();

        return dataGen.wrapByteBuffer(payload);
    }

    @Override
    public void unmarshalFlagsAndPayload(Http2Frame frame, byte flagsByte, Optional<DataWrapper> maybePayload) {
        Http2Priority castFrame = (Http2Priority) frame;

        maybePayload.ifPresent(payload -> {
            ByteBuffer payloadByteBuffer = bufferPool.createWithDataWrapper(payload);

            int firstInt = payloadByteBuffer.getInt();
            castFrame.setStreamDependencyIsExclusive((firstInt >>> 31)== 0x1);
            int streamDependency = firstInt & 0x7FFFFFFF;
            if(streamDependency == frame.getStreamId()) {
                // Can't depend on self
                throw new ParseException(Http2ErrorCode.PROTOCOL_ERROR, streamDependency, true);
            }
            castFrame.setStreamDependency(streamDependency);
            castFrame.setWeight((short) (payloadByteBuffer.get() & 0xFF));

            bufferPool.releaseBuffer(payloadByteBuffer);
        });
    }

}
