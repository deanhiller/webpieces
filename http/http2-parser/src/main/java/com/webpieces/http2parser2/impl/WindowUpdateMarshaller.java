package com.webpieces.http2parser2.impl;

import java.nio.ByteBuffer;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

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
		return super.createFrame(frame, (byte) 0, dataPayload);
	}

	@Override
	public Http2Frame unmarshal(Http2MementoImpl state, DataWrapper payload) {
		Http2WindowUpdate frame = new Http2WindowUpdate();
		super.fillInFrameHeader(state, frame);

		ByteBuffer payloadByteBuffer = bufferPool.createWithDataWrapper(payload);

		frame.setWindowSizeIncrement(payloadByteBuffer.getInt());
		bufferPool.releaseBuffer(payloadByteBuffer);
		return frame;
	}
}
