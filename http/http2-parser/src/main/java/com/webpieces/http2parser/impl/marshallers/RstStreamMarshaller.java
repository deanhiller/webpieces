package com.webpieces.http2parser.impl.marshallers;

import java.nio.ByteBuffer;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import com.webpieces.http2parser.api.Http2ParseException;
import com.webpieces.http2parser.api.ParseFailReason;
import com.webpieces.http2parser.api.dto.RstStreamFrame;
import com.webpieces.http2parser.api.dto.lib.AbstractHttp2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.impl.FrameHeaderData;
import com.webpieces.http2parser.impl.Http2MementoImpl;
import com.webpieces.http2parser.impl.UnsignedData;

public class RstStreamMarshaller extends AbstractFrameMarshaller implements FrameMarshaller {
	public RstStreamMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen) {
		super(bufferPool);
	}

	@Override
	public DataWrapper marshal(Http2Frame frame) {
		RstStreamFrame castFrame = (RstStreamFrame) frame;

		ByteBuffer payload = bufferPool.nextBuffer(4);
		UnsignedData.putUnsignedInt(payload, castFrame.getErrorCode());
		payload.flip();

		DataWrapper dataPayload = dataGen.wrapByteBuffer(payload);
		return super.marshalFrame(frame, (byte) 0, dataPayload);
	}

	@Override
	public AbstractHttp2Frame unmarshal(Http2MementoImpl state, DataWrapper framePayloadData) {
		FrameHeaderData frameHeaderData = state.getFrameHeaderData();
		int streamId = frameHeaderData.getStreamId();
		if(state.getFrameHeaderData().getPayloadLength() != 4)
            throw new Http2ParseException(ParseFailReason.FRAME_SIZE_INCORRECT_CONNECTION, streamId, 
            		"rststream size not 4 and instead is="+state.getFrameHeaderData().getPayloadLength());
		else if(frameHeaderData.getStreamId() == 0)
            throw new Http2ParseException(ParseFailReason.INVALID_STREAM_ID, frameHeaderData.getStreamId(), 
            		"rst stream cannot be streamid 0 and was="+frameHeaderData.getStreamId());
            
		RstStreamFrame frame = new RstStreamFrame();
		super.unmarshalFrame(state, frame);

		ByteBuffer payloadByteBuffer = bufferPool.createWithDataWrapper(framePayloadData);
		
		long errorCode = UnsignedData.getUnsignedInt(payloadByteBuffer);
		
		frame.setErrorCode(errorCode);

		bufferPool.releaseBuffer(payloadByteBuffer);

		return frame;
	}

}
