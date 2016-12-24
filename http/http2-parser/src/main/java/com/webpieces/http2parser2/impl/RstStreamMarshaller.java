package com.webpieces.http2parser2.impl;

import java.nio.ByteBuffer;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import com.webpieces.http2parser.api.dto.Http2Frame;
import com.webpieces.http2parser.api.dto.Http2RstStream;

public class RstStreamMarshaller extends AbstractFrameMarshaller implements FrameMarshaller {
    RstStreamMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen) {
        super(bufferPool, dataGen);
    }

	@Override
	public DataWrapper marshal(Http2Frame frame) {
        Http2RstStream castFrame = (Http2RstStream) frame;

        ByteBuffer payload = bufferPool.nextBuffer(4);
        payload.putInt(castFrame.getErrorCode().getCode());
        payload.flip();

        DataWrapper dataPayload = dataGen.wrapByteBuffer(payload);		
		return super.createFrame(frame, (byte)0, dataPayload);
	}

}
