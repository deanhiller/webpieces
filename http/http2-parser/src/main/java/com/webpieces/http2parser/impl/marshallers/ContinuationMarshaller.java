package com.webpieces.http2parser.impl.marshallers;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import com.webpieces.http2parser.api.dto.ContinuationFrame;
import com.webpieces.http2parser.api.dto.lib.AbstractHttp2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.impl.Http2MementoImpl;

public class ContinuationMarshaller extends AbstractFrameMarshaller implements FrameMarshaller {
    public ContinuationMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen) {
        super(bufferPool);
    }

	@Override
	public DataWrapper marshal(Http2Frame frame) {
        ContinuationFrame castFrame = (ContinuationFrame) frame;

        byte value = 0x0;
        if (castFrame.isEndHeaders()) value |= 0x4;
        
        DataWrapper dataPayload = castFrame.getHeaderFragment();
		return super.marshalFrame(frame, value, dataPayload);
	}

	@Override
	public AbstractHttp2Frame unmarshal(Http2MementoImpl state, DataWrapper framePayloadData) {
        ContinuationFrame frame = new ContinuationFrame();
        super.unmarshalFrame(state, frame);

        byte flags = state.getFrameHeaderData().getFlagsByte();
        frame.setEndHeaders((flags & 0x4) == 0x4);
        frame.setHeaderFragment(framePayloadData);
        
        return frame;
	}

}
