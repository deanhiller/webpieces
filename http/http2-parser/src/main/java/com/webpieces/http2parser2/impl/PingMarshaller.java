package com.webpieces.http2parser2.impl;

import java.nio.ByteBuffer;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import com.webpieces.http2parser.api.dto.Http2Frame;
import com.webpieces.http2parser.api.dto.Http2Ping;

public class PingMarshaller extends AbstractFrameMarshaller implements FrameMarshaller {

    public PingMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen) {
    	super(bufferPool, dataGen);
	}

	@Override
	public DataWrapper marshal(Http2Frame frame) {
		Http2Ping ping = (Http2Ping) frame;
        ByteBuffer payload = bufferPool.nextBuffer(8);
        payload.putLong(ping.getOpaqueData());
        payload.flip();		
		
        DataWrapper dataPayload = dataGen.wrapByteBuffer(payload);
        
        byte value = 0x0;
        if (ping.isPingResponse()) value |= 0x1;
        
		return createFrame(frame, value, dataPayload);
	}

}
