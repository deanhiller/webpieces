package com.webpieces.http2parser2.impl;

import java.nio.ByteBuffer;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import com.webpieces.http2parser.api.ParseException;
import com.webpieces.http2parser.api.dto.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.Http2Frame;
import com.webpieces.http2parser.api.dto.Http2WindowUpdate;

public class WindowUpdateMarshaller extends AbstractFrameMarshaller implements FrameMarshaller {

	WindowUpdateMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen) {
		super(bufferPool, dataGen);
	}

	@Override
	public DataWrapper marshal(Http2Frame frame) {
		Http2WindowUpdate castFrame = (Http2WindowUpdate) frame;
		ByteBuffer payload = bufferPool.nextBuffer(4).putInt(castFrame.getWindowSizeIncrement());
		payload.flip();

		DataWrapper dataPayload = dataGen.wrapByteBuffer(payload);
		return super.marshalFrame(frame, (byte) 0, dataPayload);
	}

	@Override
	public Http2Frame unmarshal(Http2MementoImpl state, DataWrapper payload) {
		FrameHeaderData frameHeaderData = state.getFrameHeaderData();
		int streamId = frameHeaderData.getStreamId();
		if(state.getFrameHeaderData().getPayloadLength() != 4)
			throw new ParseException(Http2ErrorCode.FRAME_SIZE_ERROR, streamId, "payload of window update must be exactly 4 and wasn't per http/2 spec");
		//TODO: Verify this, previous code looks like connectionlevel = false but shouldn't this be true
		
		Http2WindowUpdate frame = new Http2WindowUpdate();
		super.unmarshalFrame(state, frame);

		ByteBuffer payloadByteBuffer = bufferPool.createWithDataWrapper(payload);

		frame.setWindowSizeIncrement(payloadByteBuffer.getInt());
		bufferPool.releaseBuffer(payloadByteBuffer);
		
		if(frame.getWindowSizeIncrement() == 0)
			throw new ParseException(Http2ErrorCode.PROTOCOL_ERROR, streamId, "Window size increment cannot be 0 per http/2 spec and was");
		
		return frame;
	}
}
