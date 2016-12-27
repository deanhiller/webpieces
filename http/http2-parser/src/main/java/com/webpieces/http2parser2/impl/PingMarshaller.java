package com.webpieces.http2parser2.impl;

import java.nio.ByteBuffer;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import com.webpieces.http2parser.api.ParseException;
import com.webpieces.http2parser.api.dto.Http2ErrorCode;
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
        
		return marshalFrame(frame, value, dataPayload);
	}

	@Override
	public Http2Frame unmarshal(Http2MementoImpl state, DataWrapper framePayloadData) {
		FrameHeaderData frameHeaderData = state.getFrameHeaderData();
		int streamId = frameHeaderData.getStreamId();
		if(state.getFrameHeaderData().getPayloadLength() > 8)
			throw new ParseException(Http2ErrorCode.FRAME_SIZE_ERROR, streamId, false);
		else if(streamId != 0)
			throw new ParseException(Http2ErrorCode.PROTOCOL_ERROR, streamId, false);

		//TODO: Verify this, previous code looks like connectionlevel = false but shouldn't this be true
		
        Http2Ping frame = new Http2Ping();
		super.unmarshalFrame(state, frame);
		
		byte flags = state.getFrameHeaderData().getFlagsByte();
        frame.setIsPingResponse((flags & 0x1) == 0x1);

        ByteBuffer payloadByteBuffer = bufferPool.createWithDataWrapper(framePayloadData);
        frame.setOpaqueData(payloadByteBuffer.getLong());
        bufferPool.releaseBuffer(payloadByteBuffer);
        return frame;
	}

}
