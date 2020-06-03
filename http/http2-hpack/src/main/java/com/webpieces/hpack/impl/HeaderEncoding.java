package com.webpieces.hpack.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.twitter.hpack.Encoder;
import com.webpieces.http2.api.dto.highlevel.Http2Headers;
import com.webpieces.http2.api.dto.highlevel.Http2Push;
import com.webpieces.http2.api.dto.lowlevel.ContinuationFrame;
import com.webpieces.http2.api.dto.lowlevel.HeadersFrame;
import com.webpieces.http2.api.dto.lowlevel.PushPromiseFrame;
import com.webpieces.http2.api.dto.lowlevel.lib.HasHeaderFragment;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Frame;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;

public class HeaderEncoding {
	private static final Logger log = LoggerFactory.getLogger(HeaderEncoding.class);
    private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
    
	public List<Http2Frame> translateToFrames(long maxFrameSize, Encoder encoder, Http2Push p) {
		PushPromiseFrame frame = new PushPromiseFrame();
    	frame.setStreamId(p.getStreamId());
    	frame.setPromisedStreamId(p.getPromisedStreamId());
		List<Http2Header> headerList = p.getHeaders();
    	
    	List<Http2Frame> headerFrames = toHeaderFrames(maxFrameSize, encoder, frame, headerList);
		return headerFrames;
	}
	
	public List<Http2Frame> translateToFrames(long maxFrameSize, Encoder encoder, Http2Headers headers) {
		HeadersFrame frame = new HeadersFrame();
    	frame.setStreamId(headers.getStreamId());
    	frame.setEndOfStream(headers.isEndOfStream());
    	frame.setPriorityDetails(headers.getPriorityDetails());
    	List<Http2Header> headerList = headers.getHeaders();
		
    	List<Http2Frame> headerFrames = toHeaderFrames(maxFrameSize, encoder, frame, headerList);
		return headerFrames;
	}
	
	private List<Http2Frame> toHeaderFrames(long maxFrameSize, Encoder encoder, HasHeaderFragment firstFrame,
			List<Http2Header> headers) {
		
		if(headers.size() == 0)
			throw new IllegalArgumentException("No headers found, at least one required");
					
		List<Http2Frame> headerFrames = createHeaderFrames(firstFrame, headers, encoder, maxFrameSize);
		
		return headerFrames;
	}
    
    private List<Http2Frame> createHeaderFrames(HasHeaderFragment initialFrame, List<Http2Header> headers, Encoder encoder, long maxFrameSize) {
    	
    	int maxSize = (int) maxFrameSize;
    	if(maxFrameSize > Integer.MAX_VALUE) 
    		throw new IllegalStateException("max frame size too large for this hpack library");
    	
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

    private DataWrapper serializeHeaders(Encoder encoder, List<Http2Header> headers) {
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

	public void setMaxHeaderTableSize(Encoder encoder, int newSize) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		synchronized(encoder) {
			encoder.setMaxHeaderTableSize(out, newSize);
		}
		log.info("length of out bytes="+out.toByteArray().length);
	}
}
