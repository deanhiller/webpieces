package com.webpieces.http2parser.impl;

import com.webpieces.http2parser.api.dto.Http2Frame;
import com.webpieces.http2parser.api.dto.Http2PushPromise;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;

public class PushPromiseMarshaller extends FrameMarshallerImpl {
    PushPromiseMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen) {
        super(bufferPool, dataGen);
    }

    public DataWrapper marshalPayload(Http2Frame frame) {
        Http2PushPromise castFrame = (Http2PushPromise) frame;

        ByteBuffer prelude = bufferPool.nextBuffer(4);
        prelude.putInt(castFrame.getPromisedStreamId());
        prelude.flip();

        DataWrapper headersDW = castFrame.getHeaderBlock().serialize();
        DataWrapper finalDW = dataGen.chainDataWrappers(
                dataGen.wrapByteBuffer(prelude),
                headersDW);
        return castFrame.getPadding().padDataIfNeeded(finalDW);
    }

    public byte marshalFlags(Http2Frame frame) {
        Http2PushPromise castFrame = (Http2PushPromise) frame;

        byte value = 0x0;
        if (castFrame.isEndHeaders()) value |= 0x4;
        if (castFrame.getPadding().isPadded()) value |= 0x8;
        return value;
    }

    public void unmarshalFlagsAndPayload(Http2Frame frame, byte flags, Optional<DataWrapper> maybePayload) {
        Http2PushPromise castFrame = (Http2PushPromise) frame;

        castFrame.setEndHeaders((flags & 0x4) == 0x4);
        castFrame.getPadding().setIsPadded((flags & 0x8) == 0x8);

        maybePayload.ifPresent(payload -> {
            List<? extends DataWrapper> split = dataGen.split(payload, 4);
            ByteBuffer prelude = bufferPool.createWithDataWrapper(split.get(0));

            castFrame.setPromisedStreamId(prelude.getInt());
            castFrame.getHeaderBlock().deserialize(castFrame.getPadding().extractPayloadAndSetPaddingIfNeeded(split.get(1)));

            bufferPool.releaseBuffer(prelude);
        });
    }

}
