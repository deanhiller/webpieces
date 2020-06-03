package com.webpieces.http2parser.impl.marshallers;

import java.nio.ByteBuffer;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import com.webpieces.http2.api.dto.error.CancelReasonCode;
import com.webpieces.http2.api.dto.error.ConnectionException;
import com.webpieces.http2.api.dto.lowlevel.PingFrame;
import com.webpieces.http2.api.dto.lowlevel.lib.AbstractHttp2Frame;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Frame;
import com.webpieces.http2parser.impl.FrameHeaderData;
import com.webpieces.http2parser.impl.Http2MementoImpl;

public class PingMarshaller extends AbstractFrameMarshaller implements FrameMarshaller {

    public PingMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen) {
    	super(bufferPool);
	}

	@Override
	public DataWrapper marshal(Http2Frame frame) {
    	if(frame.getStreamId() != 0)
    		throw new IllegalArgumentException("PingFrame can never be any other stream id except 0 which is already set");
    	
		PingFrame ping = (PingFrame) frame;
        ByteBuffer payload = bufferPool.nextBuffer(8);
        payload.putLong(ping.getOpaqueData());
        payload.flip();		
		
        DataWrapper dataPayload = DATA_GEN.wrapByteBuffer(payload);
        
        byte value = 0x0;
        if (ping.isPingResponse()) value |= 0x1;
        
		return marshalFrame(frame, value, dataPayload);
	}

	@Override
	public AbstractHttp2Frame unmarshal(Http2MementoImpl state, DataWrapper framePayloadData) {
		FrameHeaderData frameHeaderData = state.getFrameHeaderData();
		int streamId = frameHeaderData.getStreamId();
		if(state.getFrameHeaderData().getPayloadLength() != 8)
            throw new ConnectionException(CancelReasonCode.FRAME_SIZE_INCORRECT, streamId, 
            		"ping size not 8 and instead is="+state.getFrameHeaderData().getPayloadLength());
		else if(streamId != 0)
			throw new ConnectionException(CancelReasonCode.INVALID_STREAM_ID, streamId, 
					"streamId on ping needs to be 0 but was="+streamId);

		//TODO: Verify this, previous code looks like connectionlevel = false but shouldn't this be true
		
        PingFrame frame = new PingFrame();
		super.unmarshalFrame(state, frame);
		
		byte flags = state.getFrameHeaderData().getFlagsByte();
        frame.setIsPingResponse((flags & 0x1) == 0x1);

        ByteBuffer payloadByteBuffer = bufferPool.createWithDataWrapper(framePayloadData);
        frame.setOpaqueData(payloadByteBuffer.getLong());
        bufferPool.releaseBuffer(payloadByteBuffer);
        
    	if(frame.getStreamId() != 0)
    		throw new IllegalArgumentException("PingFrame can never be any other stream id except 0 which is already set");
    	
        return frame;
	}

}
