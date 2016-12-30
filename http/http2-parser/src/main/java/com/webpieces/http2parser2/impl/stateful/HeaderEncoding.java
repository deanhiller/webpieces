package com.webpieces.http2parser2.impl.stateful;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;

import com.twitter.hpack.Encoder;
import com.webpieces.http2parser.api.dto.HasHeaderFragment;
import com.webpieces.http2parser.api.dto.Http2Continuation;
import com.webpieces.http2parser.api.dto.Http2Frame;
import com.webpieces.http2parser.api.dto.Http2PushPromise;
import com.webpieces.http2parser.api.dto.lib.Http2Header;

public class HeaderEncoding {
    private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private Encoder encoder;

    public HeaderEncoding(Encoder encoder) {
		this.encoder = encoder;
    }
    
    public List<Http2Frame> createPushPromises(List<Http2Header> headers, int streamId, int promisedStreamId, long maxFrameSize) {
    	Http2PushPromise promise = new Http2PushPromise();
    	promise.setStreamId(streamId);
    	promise.setPromisedStreamId(promisedStreamId);
    	
    	return createHeaderFrames(promise, headers, maxFrameSize);
    }
    
    public List<Http2Frame> createHeaderFrames(HasHeaderFragment initialFrame, List<Http2Header> headers, long maxFrameSize) {
        List<Http2Frame> headerFrames = new LinkedList<>();
    	
        DataWrapper serializedHeaders = serializeHeaders(headers);

        HasHeaderFragment currentFrame = initialFrame;
        DataWrapper dataLeftOver = serializedHeaders;
        while(dataLeftOver.getReadableSize() > 0) {
            List<? extends DataWrapper> split = dataGen.split(dataLeftOver, (int) maxFrameSize);
            DataWrapper fragment = split.get(0);
            
            currentFrame.setHeaderFragment(fragment);
            headerFrames.add(currentFrame);
            
            currentFrame = new Http2Continuation();
            currentFrame.setStreamId(initialFrame.getStreamId());
            dataLeftOver = split.get(1);
        }

        //last frame is currentFrame so set end header
        currentFrame.setEndHeaders(true);
        
		return headerFrames;
	}
    
    public DataWrapper serializeHeaders(List<Http2Header> headers) {
    	ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (Http2Header header : headers) {
            try {
                encoder.encodeHeader(
                        out,
                        header.getName().toLowerCase().getBytes(),
                        header.getValue().getBytes(),
                        false);
            } catch (IOException e) {
            	throw new RuntimeException(e);
            }
        }
        return dataGen.wrapByteArray(out.toByteArray());
    }
}
