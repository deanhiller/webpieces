package com.webpieces.http2parser.impl;

import com.webpieces.http2parser.api.FrameMarshaller;
import com.webpieces.http2parser.api.dto.Http2Frame;
import com.webpieces.http2parser.api.dto.Http2RstStream;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import java.nio.ByteBuffer;

public class RstStreamMarshaller extends FrameMarshallerImpl implements FrameMarshaller {
    RstStreamMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen) {
        super(bufferPool, dataGen);
    }

    public byte getFlagsByte(Http2Frame frame) {
        return 0x0;
    }

    public DataWrapper getPayloadDataWrapper(Http2Frame frame) {
        Http2RstStream castFrame = (Http2RstStream) frame;

        ByteBuffer payload = bufferPool.nextBuffer(4);
        payload.putInt(castFrame.getErrorCode().getCode());
        payload.flip();

        return dataGen.wrapByteBuffer(payload);
    }

}
