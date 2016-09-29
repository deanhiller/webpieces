package com.webpieces.http2parser.impl;

import com.webpieces.http2parser.api.dto.Http2Continuation;
import com.webpieces.http2parser.api.dto.Http2Frame;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

public class ContinuationMarshaller extends FrameMarshallerImpl {
    ContinuationMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen) {
        super(bufferPool, dataGen);
    }

    public byte getFlagsByte(Http2Frame frame) {
        Http2Continuation castFrame = (Http2Continuation) frame;

        byte value = 0x0;
        if (castFrame.isEndHeaders()) value |= 0x4;
        return value;
    }

    public DataWrapper getPayloadDataWrapper(Http2Frame frame) {
        Http2Continuation castFrame = (Http2Continuation) frame;
        return castFrame.getHeaderBlock().serialize();
    }
}
