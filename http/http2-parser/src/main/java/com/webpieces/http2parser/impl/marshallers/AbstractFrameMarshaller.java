package com.webpieces.http2parser.impl.marshallers;

import java.nio.ByteBuffer;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;

import com.webpieces.http2.api.dto.lowlevel.lib.Http2Frame;
import com.webpieces.http2parser.impl.FrameHeaderData;
import com.webpieces.http2parser.impl.Http2MementoImpl;

public class AbstractFrameMarshaller {
	protected static final DataWrapperGenerator DATA_GEN = DataWrapperGeneratorFactory.createDataWrapperGenerator();

    protected BufferPool bufferPool;

	public AbstractFrameMarshaller(BufferPool bufferPool) {
		this.bufferPool = bufferPool;
	}

	protected DataWrapper marshalFrame(Http2Frame frame, byte value, DataWrapper payload) {
		int originalStreamId = frame.getStreamId();
        // Clear the MSB because streamId can only be 31 bits
        int streamId = originalStreamId & 0x7FFFFFFF;
        if(streamId != originalStreamId) 
        	throw new RuntimeException("your stream id is too large per spec. frame="+frame);
		
        ByteBuffer header = ByteBuffer.allocate(9);
        
        int length = payload.getReadableSize();
        header.put((byte) (length >>> 16));
        header.putShort((short) length);

        header.put(frame.getFrameType().getId());
        header.put(value);

        // 1 bit reserved, streamId MSB is always 0, see setStreamId()
        header.putInt(streamId);
        header.flip();

        DataWrapper frameHeader = DATA_GEN.wrapByteBuffer(header);
        return DATA_GEN.chainDataWrappers(frameHeader, payload);
    }

	public void unmarshalFrame(Http2MementoImpl state, Http2Frame frame) {
		FrameHeaderData frameHeaderData = state.getFrameHeaderData();
		frame.setStreamId(frameHeaderData.getStreamId());
	}
}
