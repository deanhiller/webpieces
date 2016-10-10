package com.webpieces.http2parser.impl;

import com.webpieces.http2parser.api.FrameMarshaller;
import com.webpieces.http2parser.api.dto.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.Http2Frame;
import com.webpieces.http2parser.api.dto.Http2RstStream;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import java.nio.ByteBuffer;
import java.util.Optional;

public class RstStreamMarshaller extends FrameMarshallerImpl implements FrameMarshaller {
    RstStreamMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen) {
        super(bufferPool, dataGen);
    }

    @Override
    public byte marshalFlags(Http2Frame frame) {
        return 0x0;
    }

    @Override
    public DataWrapper marshalPayload(Http2Frame frame) {
        Http2RstStream castFrame = (Http2RstStream) frame;

        ByteBuffer payload = bufferPool.nextBuffer(4);
        payload.putInt(castFrame.getErrorCode().getCode());
        payload.flip();

        return dataGen.wrapByteBuffer(payload);
    }

    @Override
    public void unmarshalFlagsAndPayload(Http2Frame frame, byte flags, Optional<DataWrapper> maybePayload) {
        Http2RstStream castFrame = (Http2RstStream) frame;

        maybePayload.ifPresent(payload -> {
            ByteBuffer payloadByteBuffer = bufferPool.createWithDataWrapper(payload);

            castFrame.setErrorCode(Http2ErrorCode.fromInteger(payloadByteBuffer.getInt()));

            bufferPool.releaseBuffer(payloadByteBuffer);
        });
    }

}
