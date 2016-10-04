package com.webpieces.http2parser.impl;

import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.dto.Http2Continuation;
import com.webpieces.http2parser.api.dto.Http2Frame;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import javax.xml.crypto.Data;
import java.util.Optional;

public class ContinuationMarshaller extends FrameMarshallerImpl {
    private Http2Parser parser;

    ContinuationMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen, Http2Parser parser) {
        super(bufferPool, dataGen);
        this.parser = parser;
    }

    public byte marshalFlags(Http2Frame frame) {
        Http2Continuation castFrame = (Http2Continuation) frame;

        byte value = 0x0;
        if (castFrame.isEndHeaders()) value |= 0x4;
        return value;
    }

    public DataWrapper marshalPayload(Http2Frame frame) {
        Http2Continuation castFrame = (Http2Continuation) frame;
        return castFrame.getSerializedHeaders();
    }

    public void unmarshalFlagsAndPayload(Http2Frame frame, byte flags, Optional<DataWrapper> maybePayload) {
        Http2Continuation castFrame = (Http2Continuation) frame;

        castFrame.setEndHeaders((flags & 0x4) == 0x4);

        maybePayload.ifPresent(payload ->
        {
            castFrame.setSerializedHeaders(payload);
            castFrame.setHeaders(parser.deserializeHeaders(castFrame.getSerializedHeaders()));
        });
    }

}
