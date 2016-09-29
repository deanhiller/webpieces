package com.webpieces.http2parser.impl;

import com.webpieces.http2parser.api.FrameMarshaller;
import com.webpieces.http2parser.api.dto.Http2Data;
import com.webpieces.http2parser.api.dto.Http2Frame;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

public class DataMarshaller extends FrameMarshallerImpl implements FrameMarshaller  {

    DataMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen) {
        super(bufferPool, dataGen);
    }

    public DataWrapper getPayloadDataWrapper(Http2Frame frame) {
        Http2Data castFrame = (Http2Data) frame;
        return castFrame.getPadding().padDataIfNeeded(castFrame.getData());
    }

    public byte getFlagsByte(Http2Frame frame) {
        Http2Data castFrame = (Http2Data) frame;

        byte value = (byte) 0x0;
        if (castFrame.isEndStream()) value |= 0x1;
        if (castFrame.getPadding().isPadded()) value |= 0x8;
        return value;
    }
}
