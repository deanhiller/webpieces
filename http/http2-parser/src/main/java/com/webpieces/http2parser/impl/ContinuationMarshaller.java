package com.webpieces.http2parser.impl;

import java.util.Optional;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import com.webpieces.http2parser.api.dto.Http2Continuation;
import com.webpieces.http2parser.api.dto.Http2Frame;

public class ContinuationMarshaller extends FrameMarshallerImpl {
    ContinuationMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen) {
        super(bufferPool, dataGen);
    }

    @Override
    public byte marshalFlags(Http2Frame frame) {
        Http2Continuation castFrame = (Http2Continuation) frame;

        byte value = 0x0;
        if (castFrame.isEndHeaders()) value |= 0x4;
        return value;
    }

    @Override
    public DataWrapper marshalPayload(Http2Frame frame) {
        Http2Continuation castFrame = (Http2Continuation) frame;
        return castFrame.getHeaderFragment();
    }

    @Override
    public void unmarshalFlagsAndPayload(Http2Frame frame, byte flags, Optional<DataWrapper> maybePayload) {
        Http2Continuation castFrame = (Http2Continuation) frame;

        castFrame.setEndHeaders((flags & 0x4) == 0x4);

        maybePayload.ifPresent(payload ->
        {
            castFrame.setHeaderFragment(payload);
        });
    }

}
