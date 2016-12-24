package com.webpieces.http2parser2.impl;

import java.nio.ByteBuffer;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import com.webpieces.http2parser.api.dto.Http2Frame;
import com.webpieces.http2parser.api.dto.Http2GoAway;

public class GoAwayMarshaller extends AbstractFrameMarshaller implements FrameMarshaller {
    GoAwayMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen) {
        super(bufferPool, dataGen);
    }

	@Override
	public DataWrapper marshal(Http2Frame frame) {
        Http2GoAway castFrame = (Http2GoAway) frame;

        ByteBuffer prelude = bufferPool.nextBuffer(8);
        prelude
        	.putInt(castFrame.getLastStreamId())
        	.putInt(castFrame.getErrorCode().getCode());
        prelude.flip();

        DataWrapper payload = dataGen.chainDataWrappers(
                dataGen.wrapByteBuffer(prelude),
                castFrame.getDebugData()
        );		
		return super.createFrame(frame, (byte)0, payload);
	}
}
