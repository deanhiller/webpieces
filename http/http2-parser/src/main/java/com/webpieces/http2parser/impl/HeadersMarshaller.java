package com.webpieces.http2parser.impl;

import com.webpieces.http2parser.api.FrameMarshaller;
import com.webpieces.http2parser.api.dto.Http2Frame;
import com.webpieces.http2parser.api.dto.Http2Headers;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import java.nio.ByteBuffer;

public class HeadersMarshaller extends FrameMarshallerImpl implements FrameMarshaller {

    HeadersMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen) {
        super(bufferPool, dataGen);
    }

    public DataWrapper getPayloadDataWrapper(Http2Frame frame) {
        Http2Headers castFrame = (Http2Headers) frame;

        ByteBuffer prelude = bufferPool.nextBuffer(5);
        prelude.putInt(castFrame.getStreamDependency());
        if (castFrame.isStreamDependencyIsExclusive()) prelude.put(0, (byte) (prelude.get(0) | 0x80));
        prelude.put(castFrame.getWeight());
        prelude.flip();

        DataWrapper unpadded = dataGen.chainDataWrappers(
                dataGen.wrapByteBuffer(prelude),
                castFrame.getHeaderBlock().serialize());
        return castFrame.getPadding().padDataIfNeeded(unpadded);
    }

    public byte getFlagsByte(Http2Frame frame) {
        Http2Headers castFrame = (Http2Headers) frame;

        byte value = 0x0;
        if (castFrame.isEndStream()) value |= 0x1;
        if (castFrame.isEndHeaders()) value |= 0x4;
        if (castFrame.getPadding().isPadded()) value |= 0x8;
        if (castFrame.isPriority()) value |= 0x20;
        return value;
    }
}
