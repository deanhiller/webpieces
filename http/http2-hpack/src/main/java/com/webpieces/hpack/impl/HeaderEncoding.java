package com.webpieces.hpack.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.twitter.hpack.Encoder;
import com.webpieces.http2parser.api.dto.ContinuationFrame;
import com.webpieces.http2parser.api.dto.lib.HasHeaderFragment;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2Header;

public class HeaderEncoding {
	private static final Logger log = LoggerFactory.getLogger(HeaderEncoding.class);
    private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
    
    public List<Http2Frame> createHeaderFrames(HasHeaderFragment initialFrame, List<Http2Header> headers, Encoder encoder, long maxFrameSize) {
    	
    	int maxSize = (int) maxFrameSize;
    	if(maxFrameSize > Integer.MAX_VALUE) 
    		maxSize = Integer.MAX_VALUE;
    	
        List<Http2Frame> headerFrames = new LinkedList<>();
    	
        DataWrapper serializedHeaders = serializeHeaders(encoder, headers);

        HasHeaderFragment currentFrame = initialFrame;
        HasHeaderFragment lastFrame = currentFrame;
        DataWrapper dataLeftOver = serializedHeaders;
        while(dataLeftOver.getReadableSize() > 0) {
            lastFrame = currentFrame;
        	int splitSize = Math.min(dataLeftOver.getReadableSize(), maxSize);
            List<? extends DataWrapper> split = dataGen.split(dataLeftOver, splitSize);
            DataWrapper fragment = split.get(0);
            
            currentFrame.setHeaderFragment(fragment);
            headerFrames.add(currentFrame);
            
            currentFrame = new ContinuationFrame();
            currentFrame.setStreamId(initialFrame.getStreamId());
            dataLeftOver = split.get(1);
        }

        //last frame is currentFrame so set end header
        lastFrame.setEndHeaders(true);
		return headerFrames;
	}

    public DataWrapper serializeHeaders(Encoder encoder, List<Http2Header> headers) {
    	try {
			return serializeHeadersImpl(encoder, headers);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }
    
    private DataWrapper serializeHeadersImpl(Encoder encoder, List<Http2Header> headers) throws IOException {
    	ByteArrayOutputStream out = new ByteArrayOutputStream();
    	synchronized(encoder) {
	        for (Http2Header header : headers) {
	                encoder.encodeHeader(
	                        out,
	                        header.getName().toLowerCase().getBytes(),
	                        header.getValue().getBytes(),
	                        false);
	        }
    	}
        return dataGen.wrapByteArray(out.toByteArray());
    }

	public void setMaxHeaderTableSize(MarshalStateImpl state, int newSize) throws IOException {
		Encoder encoder = state.getEncoder();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		synchronized(encoder) {
			encoder.setMaxHeaderTableSize(out, newSize);
		}
		log.info("length of out bytes="+out.toByteArray().length);
	}
}
