package com.webpieces.http2parser2.impl;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import com.webpieces.http2parser.api.dto.Http2Data;
import com.webpieces.http2parser.api.dto.Http2Frame;

public class DataMarshaller extends AbstractFrameMarshaller implements FrameMarshaller  {

    DataMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen) {
        super(bufferPool, dataGen);
    }

	@Override
	public DataWrapper marshal(Http2Frame frame) {
        Http2Data castFrame = (Http2Data) frame;

        byte value = (byte) 0x0;
        if (castFrame.isEndStream()) value |= 0x1;
        if (castFrame.getPadding().isPadded()) value |= 0x8;
        
        DataWrapper dataPayload = castFrame.getPadding().padDataIfNeeded(castFrame.getData());
		return super.createFrame(frame, value, dataPayload);
	}

	@Override
	public Http2Frame unmarshal(Http2MementoImpl state, DataWrapper framePayloadData) {
        Http2Data frame = new Http2Data();
        super.fillInFrameHeader(state, frame);
        
        byte flags = state.getFrameHeaderData().getFlagsByte();
        frame.setEndStream((flags & 0x1) == 0x1);
        frame.getPadding().setIsPadded((flags & 0x8) == 0x8);

        DataWrapper data = frame.getPadding().extractPayloadAndSetPaddingIfNeeded(framePayloadData, frame.getStreamId());
        frame.setData(data);
        
		return frame;
	}
}
