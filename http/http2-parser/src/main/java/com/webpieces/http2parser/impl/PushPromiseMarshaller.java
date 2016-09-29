package com.webpieces.http2parser.impl;

import com.webpieces.http2parser.api.dto.Http2Frame;
import com.webpieces.http2parser.api.dto.Http2PushPromise;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import java.nio.ByteBuffer;

public class PushPromiseMarshaller extends FrameMarshallerImpl {
    PushPromiseMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen) {
        super(bufferPool, dataGen);
    }

    public DataWrapper getPayloadDataWrapper(Http2Frame frame) {
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

    public byte getFlagsByte(Http2Frame frame) {
        Http2PushPromise castFrame = (Http2PushPromise) frame;

        byte value = 0x0;
        if (castFrame.isEndHeaders()) value |= 0x4;
        if (castFrame.getPadding().isPadded()) value |= 0x8;
        return value;
    }

}
