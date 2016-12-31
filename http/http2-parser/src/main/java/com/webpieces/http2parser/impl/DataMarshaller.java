package com.webpieces.http2parser.impl;

import java.util.Optional;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import com.webpieces.http2parser.api.FrameMarshaller;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.lib.AbstractHttp2Frame;

public class DataMarshaller extends FrameMarshallerImpl implements FrameMarshaller  {

    DataMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen) {
        super(bufferPool, dataGen);
    }

    @Override
    public DataWrapper marshalPayload(AbstractHttp2Frame frame) {

        DataFrame castFrame = (DataFrame) frame;
        return castFrame.getPadding().padDataIfNeeded(castFrame.getData());
    }

    @Override
    public byte marshalFlags(AbstractHttp2Frame frame) {
        DataFrame castFrame = (DataFrame) frame;

        byte value = (byte) 0x0;
        if (castFrame.isEndStream()) value |= 0x1;
        if (castFrame.getPadding().isPadded()) value |= 0x8;
        return value;
    }

    @Override
    public void unmarshalFlagsAndPayload(AbstractHttp2Frame frame, byte flags, Optional<DataWrapper> maybePayload) {
        DataFrame castFrame = (DataFrame) frame;

        castFrame.setEndStream((flags & 0x1) == 0x1);
        castFrame.getPadding().setIsPadded((flags & 0x8) == 0x8);

        maybePayload.ifPresent(payload ->
                castFrame.setData(
                        castFrame.getPadding().extractPayloadAndSetPaddingIfNeeded(payload, frame.getStreamId())));
    }
}
