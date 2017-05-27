package com.webpieces.http2parser.impl.marshallers;

import java.nio.ByteBuffer;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import com.webpieces.http2parser.api.dto.PriorityFrame;
import com.webpieces.http2parser.api.dto.error.ConnectionException;
import com.webpieces.http2parser.api.dto.error.CancelReasonCode;
import com.webpieces.http2parser.api.dto.error.StreamException;
import com.webpieces.http2parser.api.dto.lib.AbstractHttp2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.api.dto.lib.PriorityDetails;
import com.webpieces.http2parser.impl.FrameHeaderData;
import com.webpieces.http2parser.impl.Http2MementoImpl;

public class PriorityMarshaller extends AbstractFrameMarshaller implements FrameMarshaller {

    public PriorityMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen) {
    	super(bufferPool);
    }

	@Override
	public DataWrapper marshal(Http2Frame frame) {
		PriorityFrame castFrame = (PriorityFrame) frame;
		
		PriorityDetails priorityDetails = castFrame.getPriorityDetails();
		
        DataWrapper payload = marshalPriorityDetails(bufferPool, priorityDetails, frame);
        
		return super.marshalFrame(frame, (byte)0, payload);
	}

	public static DataWrapper marshalPriorityDetails(BufferPool bufferPool, PriorityDetails priorityDetails, Http2Frame frame) {
		
		int streamDependency = priorityDetails.getStreamDependency();
		int newVal = streamDependency & 0x7FFFFFFF;
		if(newVal != streamDependency)
			throw new IllegalArgumentException("frame.priorityDetails.streamDependency "
					+ "is too large an id value per http/2 spec.  frame="+frame);
		
		ByteBuffer payload = bufferPool.nextBuffer(5);
        payload.putInt(priorityDetails.getStreamDependency());
        if (priorityDetails.isStreamDependencyIsExclusive()) 
        	payload.put(0, (byte) (payload.get(0) | 0x80));
        
        payload.put((byte) (priorityDetails.getWeight() & 0xFF));
        payload.flip();
        DataWrapper dataPayload = dataGen.wrapByteBuffer(payload);

		return dataPayload;
	}

	@Override
	public AbstractHttp2Frame unmarshal(Http2MementoImpl state, DataWrapper framePayloadData) {
		FrameHeaderData frameHeaderData = state.getFrameHeaderData();
		int streamId = frameHeaderData.getStreamId();
		if(state.getFrameHeaderData().getPayloadLength() != 5)
            throw new StreamException(CancelReasonCode.FRAME_SIZE_INCORRECT, streamId, 
            		"priority size not 5 and instead is="+state.getFrameHeaderData().getPayloadLength());
		
        PriorityFrame frame = new PriorityFrame();
        PriorityDetails priorityDetails = frame.getPriorityDetails();
		super.unmarshalFrame(state, frame);

        ByteBuffer payloadByteBuffer = bufferPool.createWithDataWrapper(framePayloadData);

        int firstInt = payloadByteBuffer.getInt();
        priorityDetails.setStreamDependencyIsExclusive((firstInt >>> 31)== 0x1);
        int streamDependency = firstInt & 0x7FFFFFFF;
        if(frame.getStreamId() == 0) {
            throw new ConnectionException(CancelReasonCode.INVALID_STREAM_ID, frame.getStreamId(), 
            		"priority cannot be streamid 0 and was="+frame.getStreamId());
        } else if(streamDependency == frame.getStreamId()) {
            // Can't depend on self
            throw new ConnectionException(CancelReasonCode.BAD_STREAM_DEPENDENCY, streamDependency, 
            		"stream id="+streamDependency+" depends on itself");
        }
        priorityDetails.setStreamDependency(streamDependency);
        priorityDetails.setWeight((short) (payloadByteBuffer.get() & 0xFF));

        bufferPool.releaseBuffer(payloadByteBuffer);

        return frame;
	}

}
