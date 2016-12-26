package com.webpieces.http2parser2.impl;

import java.nio.ByteBuffer;
import java.util.List;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import com.webpieces.http2parser.api.dto.Http2Frame;
import com.webpieces.http2parser.api.dto.Http2PushPromise;

public class PushPromiseMarshaller extends AbstractFrameMarshaller implements FrameMarshaller {
    PushPromiseMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen) {
        super(bufferPool, dataGen);
    }

	@Override
	public DataWrapper marshal(Http2Frame frame) {
        Http2PushPromise castFrame = (Http2PushPromise) frame;

        byte value = 0x0;
        if (castFrame.isEndHeaders()) value |= 0x4;
        if (castFrame.getPadding().isPadded()) value |= 0x8;

        ByteBuffer prelude = bufferPool.nextBuffer(4);
        prelude.putInt(castFrame.getPromisedStreamId());
        prelude.flip();

        DataWrapper headersDW = castFrame.getHeaderFragment();
        DataWrapper finalDW = dataGen.chainDataWrappers(
                dataGen.wrapByteBuffer(prelude),
                headersDW);
        DataWrapper payload = castFrame.getPadding().padDataIfNeeded(finalDW);
        
		return super.marshalFrame(frame, value, payload);
	}

	@Override
	public Http2Frame unmarshal(Http2MementoImpl state, DataWrapper framePayloadData) {
        Http2PushPromise frame = new Http2PushPromise();
		super.unmarshalFrame(state, frame);

		byte flags = state.getFrameHeaderData().getFlagsByte();
        frame.setEndHeaders((flags & 0x4) == 0x4);
        frame.getPadding().setIsPadded((flags & 0x8) == 0x8);

        List<? extends DataWrapper> split = dataGen.split(framePayloadData, 4);
        ByteBuffer prelude = bufferPool.createWithDataWrapper(split.get(0));

        frame.setPromisedStreamId(prelude.getInt());
        frame.setHeaderFragment(frame.getPadding().extractPayloadAndSetPaddingIfNeeded(split.get(1), frame.getStreamId()));
        bufferPool.releaseBuffer(prelude);
            
		return frame;
	}

}
