package com.webpieces.http2parser.impl;

import java.util.Optional;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import com.webpieces.http2parser.api.dto.ContinuationFrame;
import com.webpieces.http2parser.api.dto.lib.AbstractHttp2Frame;

public class ContinuationMarshaller extends FrameMarshallerImpl {
    ContinuationMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen) {
        super(bufferPool, dataGen);
    }

    @Override
    public byte marshalFlags(AbstractHttp2Frame frame) {
        ContinuationFrame castFrame = (ContinuationFrame) frame;

        byte value = 0x0;
        if (castFrame.isEndHeaders()) value |= 0x4;
        return value;
    }

    @Override
    public DataWrapper marshalPayload(AbstractHttp2Frame frame) {
        ContinuationFrame castFrame = (ContinuationFrame) frame;
        return castFrame.getHeaderFragment();
    }

    @Override
    public void unmarshalFlagsAndPayload(AbstractHttp2Frame frame, byte flags, Optional<DataWrapper> maybePayload) {
        ContinuationFrame castFrame = (ContinuationFrame) frame;

        castFrame.setEndHeaders((flags & 0x4) == 0x4);

        maybePayload.ifPresent(payload ->
        {
            castFrame.setHeaderFragment(payload);
        });
    }

}
