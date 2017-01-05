package com.webpieces.http2parser.impl.marshallers;

import java.nio.ByteBuffer;
import java.util.List;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import com.webpieces.http2parser.api.ParseException;
import com.webpieces.http2parser.api.dto.lib.AbstractHttp2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.impl.Http2MementoImpl;
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

        ByteBuffer prelude = bufferPool.nextBuffer(8);
        prelude
        	.putInt(castFrame.getLastStreamId())
        	.putInt(castFrame.getErrorCode().getCode());
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
            throw new ParseException(Http2ErrorCode.PROTOCOL_ERROR, streamId, true);
        
        List<? extends DataWrapper> split = dataGen.split(framePayloadData, 8);
        ByteBuffer preludeBytes = bufferPool.createWithDataWrapper(split.get(0));

        frame.setLastStreamId(preludeBytes.getInt());
        frame.setErrorCode(Http2ErrorCode.fromInteger(preludeBytes.getInt()));

        frame.setDebugData(split.get(1));

        bufferPool.releaseBuffer(preludeBytes);

		if(frame.getStreamId() != 0)
	    	throw new IllegalArgumentException("GoAwayFrame can never be any other stream id except 0 which is already set");

        return frame;
	}
}
