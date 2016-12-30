package com.webpieces.http2engine.api;

import java.util.ArrayList;
import java.util.List;

import com.webpieces.http2parser.api.dto.HasPriorityDetails.PriorityDetails;
import com.webpieces.http2parser.api.dto.lib.Http2Header;

public class Http2FullHeaders implements Http2Payload {

	private int streamId;
    private PriorityDetails priorityDetails = new PriorityDetails(); /* optional */
    private List<Http2Header> headerList = new ArrayList<>(); // only created by the parser when deserializing a bunch of header frames
	private boolean endStream;
    
    public void setStreamId(int streamId) {
        // Clear the MSB because streamId can only be 31 bits
        this.streamId = streamId & 0x7FFFFFFF;
        if(this.streamId != streamId) 
        	throw new RuntimeException("your stream id is too large");
    }

    public int getStreamId() {
        return streamId;
    }

	public PriorityDetails getPriorityDetails() {
		return priorityDetails;
	}

	public void setPriorityDetails(PriorityDetails priorityDetails) {
		this.priorityDetails = priorityDetails;
	}

	public List<Http2Header> getHeaderList() {
		return headerList;
	}

	public void setHeaderList(List<Http2Header> headerList) {
		this.headerList = headerList;
	}

	@Override
	public boolean isEndStream() {
		return endStream;
	}
	
	public void setEndStream(boolean endOfStream) {
		this.endStream = endOfStream;
	}
    
}
