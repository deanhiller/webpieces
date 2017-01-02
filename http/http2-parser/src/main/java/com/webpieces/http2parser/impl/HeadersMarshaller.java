package com.webpieces.http2parser.impl;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import com.webpieces.http2parser.api.FrameMarshaller;
import com.webpieces.http2parser.api.ParseException;
import com.webpieces.http2parser.api.dto.HeadersFrame;
import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;

public class HeadersMarshaller extends FrameMarshallerImpl implements FrameMarshaller {

    HeadersMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen) {
        super(bufferPool, dataGen);
    }

    @Override
    public DataWrapper marshalPayload(Http2Frame frame) {
        HeadersFrame castFrame = (HeadersFrame) frame;
        DataWrapper preludeDW;
        if(castFrame.isPriority()) {
            ByteBuffer prelude = bufferPool.nextBuffer(5);
            prelude.putInt(castFrame.getStreamDependency());
            if (castFrame.isStreamDependencyIsExclusive()) prelude.put(0, (byte) (prelude.get(0) | 0x80));
            prelude.put((byte) (castFrame.getWeight() & 0xFF));
            prelude.flip();
            preludeDW = dataGen.wrapByteBuffer(prelude);
        }
        else {
            preludeDW = dataGen.emptyWrapper();
        }

        DataWrapper unpadded = dataGen.chainDataWrappers(
                preludeDW,
                castFrame.getHeaderFragment());
        return castFrame.getPadding().padDataIfNeeded(unpadded);
    }

    @Override
    public byte marshalFlags(Http2Frame frame) {
        HeadersFrame castFrame = (HeadersFrame) frame;

        byte value = 0x0;
        if (castFrame.isEndStream()) value |= 0x1;
        if (castFrame.isEndHeaders()) value |= 0x4;
        if (castFrame.getPadding().isPadded()) value |= 0x8;
        if (castFrame.isPriority()) value |= 0x20;
        return value;
    }

    @Override
    public void unmarshalFlagsAndPayload(Http2Frame frame, byte flagsByte, Optional<DataWrapper> maybePayload) {
        HeadersFrame castFrame = (HeadersFrame) frame;

        castFrame.setEndStream((flagsByte & 0x1) == 0x1);
        castFrame.setEndHeaders((flagsByte & 0x4) == 0x4);
        castFrame.getPadding().setIsPadded((flagsByte & 0x8) == 0x8);
        castFrame.setPriority((flagsByte & 0x20) == 0x20);

        maybePayload.ifPresent(payload -> {
            DataWrapper paddingStripped = castFrame.getPadding().extractPayloadAndSetPaddingIfNeeded(payload, frame.getStreamId());

            if(castFrame.isPriority()) {
                List<? extends DataWrapper> split = dataGen.split(paddingStripped, 5);
                ByteBuffer preludeBytes = bufferPool.createWithDataWrapper(split.get(0));

                int firstInt = preludeBytes.getInt();
                castFrame.setStreamDependencyIsExclusive((firstInt >>> 31) == 0x1);
                int streamDependency = firstInt & 0x7FFFFFFF;
                if(streamDependency == frame.getStreamId()) {
                    // Can't depend on self
                    throw new ParseException(Http2ErrorCode.PROTOCOL_ERROR, streamDependency, true);
                }
                castFrame.setStreamDependency(streamDependency);
                castFrame.setWeight((short) (preludeBytes.get() & 0xFF));
                castFrame.setHeaderFragment(split.get(1));
                bufferPool.releaseBuffer(preludeBytes);
            } else {
                castFrame.setHeaderFragment(paddingStripped);
            }

        });
    }
}
