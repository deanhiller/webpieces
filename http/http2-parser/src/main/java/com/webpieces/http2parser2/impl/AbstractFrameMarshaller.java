package com.webpieces.http2parser2.impl;

import java.nio.ByteBuffer;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import com.webpieces.http2parser.api.dto.lib.Http2Frame;

public class AbstractFrameMarshaller {

    protected BufferPool bufferPool;
	protected DataWrapperGenerator dataGen;

	public AbstractFrameMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen) {
		this.bufferPool = bufferPool;
		this.dataGen = dataGen;
    	
	}

	protected DataWrapper marshalFrame(Http2Frame frame, byte value, DataWrapper payload) {
        ByteBuffer header = ByteBuffer.allocate(9);
        
        int length = payload.getReadableSize();
        header.put((byte) (length >>> 16));
        header.putShort((short) length);

        header.put(frame.getFrameType().getId());
        header.put(value);

        // 1 bit reserved, streamId MSB is always 0, see setStreamId()
        header.putInt(frame.getStreamId());
        header.flip();

        DataWrapper frameHeader = dataGen.wrapByteBuffer(header);
        return dataGen.chainDataWrappers(frameHeader, payload);
    }

	public void unmarshalFrame(Http2MementoImpl state, Http2Frame frame) {
		FrameHeaderData frameHeaderData = state.getFrameHeaderData();
		frame.setStreamId(frameHeaderData.getStreamId());
	}
}
