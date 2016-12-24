package com.webpieces.http2parser2.impl;

import java.nio.ByteBuffer;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

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
}
