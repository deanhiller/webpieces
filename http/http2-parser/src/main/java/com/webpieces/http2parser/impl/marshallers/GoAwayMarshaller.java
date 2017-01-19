package com.webpieces.http2parser.impl.marshallers;

import java.nio.ByteBuffer;
import java.util.List;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import com.webpieces.http2parser.api.Http2ParseException;
import com.webpieces.http2parser.api.dto.lib.AbstractHttp2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.impl.Http2MementoImpl;
import com.webpieces.http2parser.impl.UnsignedData;
import com.webpieces.http2parser.api.dto.GoAwayFrame;

public class GoAwayMarshaller extends AbstractFrameMarshaller implements FrameMarshaller {
    public GoAwayMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen) {
        super(bufferPool);
    }

	@Override
	public DataWrapper marshal(Http2Frame frame) {
		if(frame.getStreamId() != 0)
	    	throw new IllegalArgumentException("GoAwayFrame can never be any other stream id except 0 which is already set");

        GoAwayFrame castFrame = (GoAwayFrame) frame;

        long originalStreamId = castFrame.getLastStreamId();
        long streamId = originalStreamId & 0x7FFFFFFF;
        if(streamId != originalStreamId) 
        	throw new RuntimeException("your lastStreamId is too large per spec. frame="+frame);
        
        ByteBuffer prelude = bufferPool.nextBuffer(8);
        UnsignedData.putUnsignedInt(prelude, castFrame.getLastStreamId());
        UnsignedData.putUnsignedInt(prelude, castFrame.getErrorCode());
        prelude.flip();

        DataWrapper payload = dataGen.chainDataWrappers(
                dataGen.wrapByteBuffer(prelude),
                castFrame.getDebugData()
        );		
		return super.marshalFrame(frame, (byte)0, payload);
	}

	@Override
	public AbstractHttp2Frame unmarshal(Http2MementoImpl state, DataWrapper framePayloadData) {
        GoAwayFrame frame = new GoAwayFrame();
        super.unmarshalFrame(state, frame);
        int streamId = state.getFrameHeaderData().getStreamId();
        if(streamId != 0)
            throw new Http2ParseException(Http2ErrorCode.PROTOCOL_ERROR, streamId, true);
        
        List<? extends DataWrapper> split = dataGen.split(framePayloadData, 8);
        ByteBuffer preludeBytes = bufferPool.createWithDataWrapper(split.get(0));

        long lastStreamId = UnsignedData.getUnsignedInt(preludeBytes);
        long errorCode = UnsignedData.getUnsignedInt(preludeBytes);
        
        frame.setLastStreamId(lastStreamId);
        frame.setErrorCode(errorCode);

        frame.setDebugData(split.get(1));

        bufferPool.releaseBuffer(preludeBytes);

		if(frame.getStreamId() != 0)
	    	throw new IllegalArgumentException("GoAwayFrame can never be any other stream id except 0 which is already set");

        return frame;
	}
}
