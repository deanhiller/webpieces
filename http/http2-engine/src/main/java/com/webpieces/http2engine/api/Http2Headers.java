package com.webpieces.http2engine.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.HasPriorityDetails.PriorityDetails;

public class Http2Headers implements PartialStream {

	private int streamId;
	private boolean endOfStream = false;
    private PriorityDetails priorityDetails = new PriorityDetails(); /* optional */
	protected List<Http2Header> headers = new ArrayList<>();
	//Convenience structure that further morphs the headers into a Map that can
	//be looked up by key.
	private transient Http2HeaderStruct headersStruct = new Http2HeaderStruct();

	public Http2Headers() {}
	public Http2Headers(List<Http2Header> headerList) {
		for(Http2Header header : headerList) {
			addHeader(header);
		}
	}

	public int getStreamId() {
		return streamId;
	}

	public void setStreamId(int streamId) {
		this.streamId = streamId;
	}
	
	/**
	 * Order of HTTP Headers matters for Headers with the same key
	 */
	public List<Http2Header> getHeaders() {
		return Collections.unmodifiableList(headers);
	}

	public void addHeader(Http2Header header) {
		headers.add(header);
		headersStruct.addHeader(header);
	}
	
	/** 
	 * 
	 * @return
	 */
	public Http2HeaderStruct getHeaderLookupStruct() {
		return headersStruct;
	}
	public boolean isEndOfStream() {
		return endOfStream;
	}
	public void setEndOfStream(boolean lastPartOfResponse) {
		this.endOfStream = lastPartOfResponse;
	}
	
	@Override
	public String toString() {
		return "Http2Headers [streamId=" + streamId + ", endStream=" + endOfStream + ", headerList="
				+ headers + ", priorityDetails=" + getPriorityDetails() + "]";
	}
	public PriorityDetails getPriorityDetails() {
		return priorityDetails;
	}
	public void setPriorityDetails(PriorityDetails priorityDetails) {
		this.priorityDetails = priorityDetails;
	}
}
