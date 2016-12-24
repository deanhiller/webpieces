package com.webpieces.http2parser2.impl;

import java.nio.ByteBuffer;
import java.util.List;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import com.webpieces.http2parser.api.ParseException;
import com.webpieces.http2parser.api.dto.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.Http2Frame;
import com.webpieces.http2parser.api.dto.Http2Headers;

public class HeadersMarshaller extends AbstractFrameMarshaller implements FrameMarshaller {

    HeadersMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen) {
        super(bufferPool, dataGen);
    }

	@Override
	public DataWrapper marshal(Http2Frame frame) {
        Http2Headers castFrame = (Http2Headers) frame;

        byte value = 0x0;
        if (castFrame.isEndStream()) value |= 0x1;
        if (castFrame.isEndHeaders()) value |= 0x4;
        if (castFrame.getPadding().isPadded()) value |= 0x8;
        if (castFrame.isPriority()) value |= 0x20;
        
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
        DataWrapper payload = castFrame.getPadding().padDataIfNeeded(unpadded);        
        return super.createFrame(frame, value, payload);
	}

	@Override
	public Http2Frame unmarshal(Http2MementoImpl state, DataWrapper framePayloadData) {
        Http2Headers frame = new Http2Headers();
        super.fillInFrameHeader(state, frame);

        byte flagsByte = state.getFrameHeaderData().getFlagsByte();
        frame.setEndStream((flagsByte & 0x1) == 0x1);
        frame.setEndHeaders((flagsByte & 0x4) == 0x4);
        frame.getPadding().setIsPadded((flagsByte & 0x8) == 0x8);
        frame.setPriority((flagsByte & 0x20) == 0x20);

        DataWrapper paddingStripped = frame.getPadding().extractPayloadAndSetPaddingIfNeeded(framePayloadData, frame.getStreamId());

        if(frame.isPriority()) {
            List<? extends DataWrapper> split = dataGen.split(paddingStripped, 5);
            ByteBuffer preludeBytes = bufferPool.createWithDataWrapper(split.get(0));

            int firstInt = preludeBytes.getInt();
            frame.setStreamDependencyIsExclusive((firstInt >>> 31) == 0x1);
            int streamDependency = firstInt & 0x7FFFFFFF;
            if(streamDependency == frame.getStreamId()) {
                // Can't depend on self
                throw new ParseException(Http2ErrorCode.PROTOCOL_ERROR, streamDependency, true);
            }
            frame.setStreamDependency(streamDependency);
            frame.setWeight((short) (preludeBytes.get() & 0xFF));
            frame.setHeaderFragment(split.get(1));
            bufferPool.releaseBuffer(preludeBytes);
        } else {
            frame.setHeaderFragment(paddingStripped);
        }
        
        return frame;
	}
}
