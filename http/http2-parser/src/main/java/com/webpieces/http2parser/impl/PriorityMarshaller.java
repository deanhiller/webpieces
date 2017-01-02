package com.webpieces.http2parser.impl;

import java.nio.ByteBuffer;
import java.util.Optional;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import com.webpieces.http2parser.api.FrameMarshaller;
import com.webpieces.http2parser.api.ParseException;
import com.webpieces.http2parser.api.dto.PriorityFrame;
import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.api.dto.lib.PriorityDetails;

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
        PriorityFrame castFrame = (PriorityFrame) frame;

        PriorityDetails priorityDetails = castFrame.getPriorityDetails();
        
        ByteBuffer payload = bufferPool.nextBuffer(5);
        payload.putInt(priorityDetails.getStreamDependency());
        if (priorityDetails.isStreamDependencyIsExclusive()) payload.put(0, (byte) (payload.get(0) | 0x80));
        payload.put((byte) (priorityDetails.getWeight() & 0xFF));
        payload.flip();

        return dataGen.wrapByteBuffer(payload);
    }

    @Override
    public void unmarshalFlagsAndPayload(Http2Frame frame, byte flagsByte, Optional<DataWrapper> maybePayload) {
        PriorityFrame castFrame = (PriorityFrame) frame;

        maybePayload.ifPresent(payload -> {
            ByteBuffer payloadByteBuffer = bufferPool.createWithDataWrapper(payload);

            PriorityDetails priorityDetails = castFrame.getPriorityDetails();

            int firstInt = payloadByteBuffer.getInt();
            priorityDetails.setStreamDependencyIsExclusive((firstInt >>> 31)== 0x1);
            int streamDependency = firstInt & 0x7FFFFFFF;
            if(streamDependency == frame.getStreamId()) {
                // Can't depend on self
                throw new ParseException(Http2ErrorCode.PROTOCOL_ERROR, streamDependency, true);
            }
            priorityDetails.setStreamDependency(streamDependency);
            priorityDetails.setWeight((short) (payloadByteBuffer.get() & 0xFF));

            bufferPool.releaseBuffer(payloadByteBuffer);
        });
    }

}
