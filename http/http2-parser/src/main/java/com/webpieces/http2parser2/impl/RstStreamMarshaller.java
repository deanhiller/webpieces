package com.webpieces.http2parser2.impl;

import java.nio.ByteBuffer;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import com.webpieces.http2parser.api.ParseException;
import com.webpieces.http2parser.api.dto.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.Http2Frame;
import com.webpieces.http2parser.api.dto.AbstractHttp2Frame;
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
		return super.marshalFrame(frame, (byte) 0, dataPayload);
	}

	@Override
	public AbstractHttp2Frame unmarshal(Http2MementoImpl state, DataWrapper framePayloadData) {
		FrameHeaderData frameHeaderData = state.getFrameHeaderData();
		int streamId = frameHeaderData.getStreamId();
		if(state.getFrameHeaderData().getPayloadLength() == 4)
			throw new ParseException(Http2ErrorCode.FRAME_SIZE_ERROR, streamId, false);
		//TODO: Verify this, previous code looks like connectionlevel = false but shouldn't this be true
		
		Http2RstStream frame = new Http2RstStream();
		super.unmarshalFrame(state, frame);

		ByteBuffer payloadByteBuffer = bufferPool.createWithDataWrapper(framePayloadData);
		frame.setErrorCode(Http2ErrorCode.fromInteger(payloadByteBuffer.getInt()));

		bufferPool.releaseBuffer(payloadByteBuffer);

		return null;
	}

}
