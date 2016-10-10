package com.webpieces.http2parser.impl;

import com.webpieces.http2parser.api.FrameMarshaller;
import com.webpieces.http2parser.api.dto.Http2Data;
import com.webpieces.http2parser.api.dto.Http2Frame;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import java.util.Optional;

public class DataMarshaller extends FrameMarshallerImpl implements FrameMarshaller  {

    DataMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen) {
        super(bufferPool, dataGen);
    }

    @Override
    public DataWrapper marshalPayload(Http2Frame frame) {

        Http2Data castFrame = (Http2Data) frame;
        return castFrame.getPadding().padDataIfNeeded(castFrame.getData());
    }

    @Override
    public byte marshalFlags(Http2Frame frame) {
        Http2Data castFrame = (Http2Data) frame;

        byte value = (byte) 0x0;
        if (castFrame.isEndStream()) value |= 0x1;
        if (castFrame.getPadding().isPadded()) value |= 0x8;
        return value;
    }

    @Override
    public void unmarshalFlagsAndPayload(Http2Frame frame, byte flags, Optional<DataWrapper> maybePayload) {
        Http2Data castFrame = (Http2Data) frame;

        castFrame.setEndStream((flags & 0x1) == 0x1);
        castFrame.getPadding().setIsPadded((flags & 0x8) == 0x8);

        maybePayload.ifPresent(payload ->
                castFrame.setData(
                        castFrame.getPadding().extractPayloadAndSetPaddingIfNeeded(payload)));
    }
}
