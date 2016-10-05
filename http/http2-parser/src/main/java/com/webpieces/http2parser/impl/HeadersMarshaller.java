package com.webpieces.http2parser.impl;

import com.webpieces.http2parser.api.FrameMarshaller;
import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.dto.Http2Frame;
import com.webpieces.http2parser.api.dto.Http2Headers;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;

public class HeadersMarshaller extends FrameMarshallerImpl implements FrameMarshaller {

    Http2Parser parser;

    HeadersMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen, Http2Parser parser) {
        super(bufferPool, dataGen);
        this.parser = parser;
    }

    public DataWrapper marshalPayload(Http2Frame frame) {
        Http2Headers castFrame = (Http2Headers) frame;

        ByteBuffer prelude = bufferPool.nextBuffer(5);
        prelude.putInt(castFrame.getStreamDependency());
        if (castFrame.isStreamDependencyIsExclusive()) prelude.put(0, (byte) (prelude.get(0) | 0x80));
        prelude.put(castFrame.getWeight());
        prelude.flip();

        DataWrapper unpadded = dataGen.chainDataWrappers(
                dataGen.wrapByteBuffer(prelude),
                castFrame.getHeaderFragment());
        return castFrame.getPadding().padDataIfNeeded(unpadded);
    }

    public byte marshalFlags(Http2Frame frame) {
        Http2Headers castFrame = (Http2Headers) frame;

        byte value = 0x0;
        if (castFrame.isEndStream()) value |= 0x1;
        if (castFrame.isEndHeaders()) value |= 0x4;
        if (castFrame.getPadding().isPadded()) value |= 0x8;
        if (castFrame.isPriority()) value |= 0x20;
        return value;
    }

    public void unmarshalFlagsAndPayload(Http2Frame frame, byte flagsByte, Optional<DataWrapper> maybePayload) {
        Http2Headers castFrame = (Http2Headers) frame;

        castFrame.setEndStream((flagsByte & 0x1) == 0x1);
        castFrame.setEndHeaders((flagsByte & 0x4) == 0x4);
        castFrame.getPadding().setIsPadded((flagsByte & 0x8) == 0x8);
        castFrame.setPriority((flagsByte & 0x20) == 0x20);

        maybePayload.ifPresent(payload -> {
            List<? extends DataWrapper> split = dataGen.split(payload, 5);
            ByteBuffer preludeBytes = bufferPool.createWithDataWrapper(split.get(0));

            int firstInt = preludeBytes.getInt();
            castFrame.setStreamDependencyIsExclusive((firstInt >>> 31) == 0x1);
            castFrame.setStreamDependency(firstInt & 0x7FFFFFFF);
            castFrame.setWeight(preludeBytes.get());
            castFrame.setHeaderFragment(castFrame.getPadding().extractPayloadAndSetPaddingIfNeeded(split.get(1)));

            bufferPool.releaseBuffer(preludeBytes);
        });
    }
}
