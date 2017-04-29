package com.webpieces.http2parser.impl.marshallers;

import java.nio.ByteBuffer;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import com.webpieces.http2parser.api.Http2ParseException;
import com.webpieces.http2parser.api.ParseFailReason;
import com.webpieces.http2parser.api.dto.WindowUpdateFrame;
import com.webpieces.http2parser.api.dto.lib.AbstractHttp2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.impl.FrameHeaderData;
import com.webpieces.http2parser.impl.Http2MementoImpl;

public class WindowUpdateMarshaller extends AbstractFrameMarshaller implements FrameMarshaller {

	public WindowUpdateMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen) {
		super(bufferPool);
	}

	@Override
	public DataWrapper marshal(Http2Frame frame) {
		WindowUpdateFrame castFrame = (WindowUpdateFrame) frame;
		ByteBuffer payload = bufferPool.nextBuffer(4).putInt(castFrame.getWindowSizeIncrement());
		payload.flip();

		DataWrapper dataPayload = dataGen.wrapByteBuffer(payload);
		return super.marshalFrame(frame, (byte) 0, dataPayload);
	}

	@Override
	public AbstractHttp2Frame unmarshal(Http2MementoImpl state, DataWrapper payload) {
		FrameHeaderData frameHeaderData = state.getFrameHeaderData();
		int streamId = frameHeaderData.getStreamId();
		if(state.getFrameHeaderData().getPayloadLength() != 4)
            throw new Http2ParseException(ParseFailReason.FRAME_SIZE_INCORRECT_CONNECTION, streamId, 
            		"window update size not 4 and instead is="+state.getFrameHeaderData().getPayloadLength());
		
		WindowUpdateFrame frame = new WindowUpdateFrame();
		super.unmarshalFrame(state, frame);

		ByteBuffer payloadByteBuffer = bufferPool.createWithDataWrapper(payload);

		frame.setWindowSizeIncrement(payloadByteBuffer.getInt());
		bufferPool.releaseBuffer(payloadByteBuffer);
		
		if(frame.getWindowSizeIncrement() == 0)
            throw new Http2ParseException(ParseFailReason.WINDOW_SIZE_INVALID, streamId, 
            		"Window size increment cannot be 0 per http/2 spec and was");
		
		return frame;
	}
}
