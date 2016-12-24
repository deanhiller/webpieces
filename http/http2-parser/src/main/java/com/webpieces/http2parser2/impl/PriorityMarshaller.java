package com.webpieces.http2parser2.impl;

import java.nio.ByteBuffer;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import com.webpieces.http2parser.api.ParseException;
import com.webpieces.http2parser.api.dto.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.Http2Frame;
import com.webpieces.http2parser.api.dto.Http2Priority;

public class PriorityMarshaller extends AbstractFrameMarshaller implements FrameMarshaller {

    PriorityMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen) {
    	super(bufferPool, dataGen);
    }

	@Override
	public DataWrapper marshal(Http2Frame frame) {
		Http2Priority castFrame = (Http2Priority) frame;
        ByteBuffer payload = bufferPool.nextBuffer(5);
        payload.putInt(castFrame.getStreamDependency());
        if (castFrame.isStreamDependencyIsExclusive()) 
        	payload.put(0, (byte) (payload.get(0) | 0x80));
        
        payload.put((byte) (castFrame.getWeight() & 0xFF));
        payload.flip();

        DataWrapper dataPayload = dataGen.wrapByteBuffer(payload);
        
		return super.createFrame(frame, (byte)0, dataPayload);
	}

	@Override
	public Http2Frame unmarshal(Http2MementoImpl state, DataWrapper framePayloadData) {
        Http2Priority frame = new Http2Priority();
		super.fillInFrameHeader(state, frame);

        ByteBuffer payloadByteBuffer = bufferPool.createWithDataWrapper(framePayloadData);

        int firstInt = payloadByteBuffer.getInt();
        frame.setStreamDependencyIsExclusive((firstInt >>> 31)== 0x1);
        int streamDependency = firstInt & 0x7FFFFFFF;
        if(streamDependency == frame.getStreamId()) {
            // Can't depend on self
            throw new ParseException(Http2ErrorCode.PROTOCOL_ERROR, streamDependency, true);
        }
        frame.setStreamDependency(streamDependency);
        frame.setWeight((short) (payloadByteBuffer.get() & 0xFF));

        bufferPool.releaseBuffer(payloadByteBuffer);

        return frame;
	}

}
